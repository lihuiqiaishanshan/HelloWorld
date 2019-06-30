package com.hisilicon.tvui.installtion;

public enum EnBandwidth
{
    UNDEFINED(0), BW5000(5000), BW6000(6000), BW7000(7000), BW8000(8000);

    private int mIndex = 0;

    EnBandwidth(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static EnBandwidth valueOf(int value)
    {
        EnBandwidth ret = UNDEFINED;
        switch (value)
        {
        case 5000:
        {
            ret = BW5000;
            break;
        }
        case 6000:
        {
            ret = BW6000;
            break;

        }
        case 7000:
        {
            ret = BW7000;
            break;
        }
        case 8000:
        {
            ret = BW8000;
            break;
        }
        default:
            ret = BW8000;
        }
        return ret;
    }
}

