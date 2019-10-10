package org.rdengine.net;

public interface ITypeDef
{
    // enum dm_network_type
    /** 未知. */
    int DM_NETWORK_TYPE_UNKNOWN = 0; // /< 未知

    /** 无网络连接. */
    int DM_NETWORK_TYPE_NONE = 1; // /< 无网络连接

    /** cmwap. */
    int DM_NETWORK_TYPE_CMWAP = 2; // /<

    /** ctwap. */
    int DM_NETWORK_TYPE_CTWAP = 3; // /<

    /** 2G. */
    int DM_NETWORK_TYPE_2G = 4; // /< 2G

    /** 3G. */
    int DM_NETWORK_TYPE_3G = 5; // /< 3G

    /** WIFI. */
    int DM_NETWORK_TYPE_WIFI = 6; // /< WIFI

    /** 有线. */
    int DM_NETWORK_TYPE_WIRED = 7; // /< 有线
}
