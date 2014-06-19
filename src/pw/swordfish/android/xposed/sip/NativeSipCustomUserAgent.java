package pw.swordfish.android.xposed.sip;

import android.net.sip.SipProfile;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import pw.swordfish.util.function.Suppliers;
import pw.swordfish.util.function.Supplier;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class NativeSipCustomUserAgent implements IXposedHookLoadPackage {
    private static final String expectedPackage = "com.android.phone";
    private static final String userAgentHeader = "User-Agent";
    private static final String userAgent = "NT_DUO IVer34-13.3";

    private final Supplier<HeaderFactory> mHeaderFactory = Suppliers.memoize(new Supplier<HeaderFactory>() {
        @Override
        public HeaderFactory get() {
            try {
                return SipFactory.getInstance().createHeaderFactory();
            } catch (PeerUnavailableException e) {
                return null;
            }
        }
    });

    @SuppressWarnings("unchecked")
    private static <T> T unsafeCast(Object o) {
        return (T) o;
    }

	public void handleLoadPackage(LoadPackageParam param) throws Throwable {
        if (!param.packageName.equals(expectedPackage))
            return;
        findAndHookMethod("com.android.server.sip.SipHelper", param.classLoader, "createRequest", String.class, SipProfile.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Request request = NativeSipCustomUserAgent.unsafeCast(param.getResult());
                request.removeHeader(userAgentHeader);
                request.addHeader(mHeaderFactory.get().createHeader(
                        userAgentHeader, userAgent));
            }
        });
        findAndHookMethod("com.android.server.sip.SipHelper", param.classLoader, "createRequest", String.class, SipProfile.class, SipProfile.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Request request = NativeSipCustomUserAgent.unsafeCast(param.getResult());
                request.addHeader(mHeaderFactory.get().createHeader(
                        userAgentHeader, userAgent));
            }
        });
	}
}
