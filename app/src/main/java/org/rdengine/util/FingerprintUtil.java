package org.rdengine.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

/**
 * Created by CCCMAX on 2019/5/10.
 */

public class FingerprintUtil
{
    private final FingerprintManagerCompat fingerprintManager;
    private final KeyguardManager keyguardManager;

    private FingerprintUtil(Context context)
    {
        // fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        fingerprintManager = FingerprintManagerCompat.from(context);
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    private static FingerprintUtil singleton = null;

    public static FingerprintUtil getInstance(Context context)
    {
        if (singleton == null)
        {
            synchronized (FingerprintUtil.class)
            {
                if (singleton == null)
                {
                    singleton = new FingerprintUtil(context);
                }
            }
        }
        return singleton;
    }

    public boolean isEnabled()
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return isHardFinger() && isWindowSafe() && isHaveHandler();
        } catch (Exception ex)
        {
        }
        return false;
    }

    /**
     * ②检查手机硬件（有没有指纹感应区）
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isHardFinger()
    {
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected())
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * ③检查手机是否开启锁屏密码
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isWindowSafe()
    {
        if (keyguardManager != null && keyguardManager.isKeyguardSecure())
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * ④检查手机是否已录入指纹
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isHaveHandler()
    {
        if (fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints())
        {
            return true;
        } else
        {
            return false;
        }

    }

    /**
     * 创建指纹验证<br>
     * 参数中最重要的就是 cancellationSignal和 callback，其他传null 和 0 就行，<br>
     * cancellationsignal 是用来取消指纹验证的，而callback 可以回调 指纹验证失败次数 或者指纹验证成功
     */
    // @RequiresApi(api = Build.VERSION_CODES.M)
    public void authenticate(FingerprintManagerCompat.CryptoObject cryptoObject, CancellationSignal cancellationSignal,
            int flag, FingerprintManagerCompat.AuthenticationCallback authenticationCallback, Handler handler)
    {
        if (fingerprintManager != null)
        {
            fingerprintManager.authenticate(cryptoObject, flag, cancellationSignal, authenticationCallback, handler);
        }
    }

}
