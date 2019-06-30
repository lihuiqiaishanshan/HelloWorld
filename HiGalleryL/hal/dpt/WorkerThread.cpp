#include <utils/Looper.h>
#include "WorkerThread.h"
#include "android/looper.h"

#define MSG_WORK 0x100

android::sp<android::Looper> mLooper;

void *(*worker_routine) (void *);
void *worker_arg = NULL;

void *worker_thread(void *)
{
    mLooper = new android::Looper(true);
    int result;

    while(true)
    {
        result = mLooper->pollOnce(1000);
        if(result == android::Looper::POLL_ERROR)
        {
            break;
        }
    }

    return NULL;
}

int worker_thread_init(void)
{
    int err = 0;
    if(NULL != mLooper.get())
    {
        return -1;
    }

    worker_routine = NULL;
    pthread_t p;
    err = pthread_create(&p, NULL, worker_thread, NULL);
    if(err)
    {
        ALOGE("ERR :(%s) LINE: %d | pthread_create failed (%d) ", __FUNCTION__, __LINE__, err);
        return err;
    }
    return 0;
}

struct worker_Message {
    struct android::Message msg;
};

class workerMessageHandler : public android::MessageHandler {
public:
    virtual void handleMessage(const android::Message& message) {
        struct worker_Message *wm = (struct worker_Message*)&message;
        if(worker_routine != NULL)
        {
            void *ret = worker_routine(worker_arg);
        }
    }
};

int worker_thread_msg(void *(*wr) (void *), void *warg)
{
    struct worker_Message wm;
    wm.msg.what = MSG_WORK;
    worker_routine = wr;
    worker_arg = warg;
    android::sp<workerMessageHandler> handler = new workerMessageHandler();

    if(mLooper != NULL)
    {
        mLooper->sendMessage(handler, wm.msg);
    }
    return 0;
}

