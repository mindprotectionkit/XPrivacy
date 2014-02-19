package biz.bokhorst.xprivacy;

import java.lang.reflect.Method;

import android.os.IBinder;
import android.util.Log;

import com.saurik.substrate.MS;

public class Cydia {
	static void initialize() {
		Util.log(null, Log.WARN, "Cydia");
		MS.hookClassLoad("android.os.ServiceManager", new MS.ClassLoadHook() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void classLoaded(Class<?> serviceManager) {
				try {
					Util.log(null, Log.WARN, "Cydia: ServiceManager loaded");
					Method addService = serviceManager.getMethod("addService", String.class, IBinder.class,
							boolean.class);
					final MS.MethodPointer old = new MS.MethodPointer();

					MS.hookMethod(serviceManager, addService, new MS.MethodHook() {
						public Object invoked(Object serviceManager, Object... args) throws Throwable {
							Util.log(null, Log.WARN, "Cydia: addService" + args[0]);
							// Before
							return old.invoke(serviceManager, args);
							// After
						}
					}, old);
				} catch (Throwable ex) {
					Util.bug(null, ex);
				}
			}
		});
	}
}
