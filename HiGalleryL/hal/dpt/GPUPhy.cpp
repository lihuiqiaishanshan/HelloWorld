#include "gralloc_priv.h"

int getGPUBufferPhy(struct private_handle_t* handle)
{
    return handle->ion_phy_addr;
}

