//#include <stdlib.h>
//#include <stdio.h>
#include <sys/mman.h>
#include <ion/ion.h>
#include <linux/ion.h>

#include <securec.h>

#include <utils/Log.h>

using namespace android;

int main(void)
{

    int fd = -1;
    int shared_fd = -1;

    ion_user_handle_t ion_hnd;
    unsigned char *vaddr;
    uint32_t phyaddr;
    size_t size = 4000*3000*4;
    size_t len = 0;

    fd = ion_open();

    int ret = ion_alloc(fd, size, 0, ION_HEAP(ION_HIS_ID_DDR), 0, &ion_hnd);
    ALOGE("ion_alloc heap id :%d", ION_HEAP(ION_HIS_ID_DDR));
    if(ret)
    {
        ALOGE("ion_alloc failed");
        return ret;
    }

    ret = ion_share(fd, ion_hnd, &shared_fd);
    if(ret)
    {
        ALOGE("ion_share failed");
        return ret;
    }

    vaddr = (unsigned char*)mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, shared_fd, 0);
    if(MAP_FAILED == vaddr)
    {
        ALOGE("mmap Failed");
        return ret;
    }

    if (EOK != memset_s(vaddr, size, 0 , size)){
        ALOGE("memset_s failed !");
    }
    ret = ion_phys(fd, ion_hnd, &phyaddr, &len);
    if(ret)
    {
        ALOGE("ion_phys failed.");
        return -1;
    }
    ALOGE("ion_phys success phyaddr %x  len %ld", phyaddr, len);
    return 0;

}
