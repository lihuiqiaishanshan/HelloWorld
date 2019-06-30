#ifndef __WORKER_THREAD_H
#define __WORKER_THREAD_H
extern "C" {
int worker_thread_init(void);
int worker_thread_msg(void *(*worker_routine) (void *), void *arg);
}
#endif /*__WORKER_THREAD_H*/
