package biz.bokhorst.xprivacy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

public class XRuntime extends XHook {
	private Methods mMethod;
	private String mCommand;

	private XRuntime(Methods method, String restrictionName, String command) {
		super(restrictionName, method.name(), command);
		mMethod = method;
		mCommand = command;
	}

	public String getClassName() {
		return "java.lang.Runtime";
	}

	@Override
	public boolean isVisible() {
		return !(mMethod == Methods.load || mMethod == Methods.loadLibrary);
	}

	// public Process exec(String[] progArray)
	// public Process exec(String[] progArray, String[] envp)
	// public Process exec(String[] progArray, String[] envp, File directory)
	// public Process exec(String prog)
	// public Process exec(String prog, String[] envp)
	// public Process exec(String prog, String[] envp, File directory)
	// public void load(String pathName)
	// public void loadLibrary(String libName)
	// libcore/luni/src/main/java/java/lang/Runtime.java
	// http://developer.android.com/reference/java/lang/Runtime.html

	private enum Methods {
		exec, load, loadLibrary
	};

	public static List<XHook> getInstances() {
		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XRuntime(Methods.exec, PrivacyManager.cShell, "sh"));
		listHook.add(new XRuntime(Methods.exec, PrivacyManager.cShell, "su"));
		listHook.add(new XRuntime(Methods.exec, PrivacyManager.cShell, null));
		listHook.add(new XRuntime(Methods.load, PrivacyManager.cShell, null));
		listHook.add(new XRuntime(Methods.loadLibrary, PrivacyManager.cShell, null));
		return listHook;
	}

	@Override
	protected void before(XParam param) throws Throwable {
		if (mMethod == Methods.exec) {
			// Get programs
			String[] progs = null;
			if (param.args.length > 0 && param.args[0] != null)
				if (String.class.isAssignableFrom(param.args[0].getClass()))
					progs = new String[] { (String) param.args[0] };
				else
					progs = (String[]) param.args[0];

			// Check programs
			if (progs != null) {
				String command = TextUtils.join(" ", progs);
				if (mCommand == null ? !(command.startsWith("sh") || command.startsWith("su")
						|| command.contains("sh ") || command.contains("su ")) : command.startsWith(mCommand)
						|| command.contains(mCommand + " "))
					if (isRestrictedExtra(param, command))
						param.setThrowable(new IOException());
			}

		} else if (mMethod == Methods.load || mMethod == Methods.loadLibrary) {
			if (isRestrictedExtra(param, (String) param.args[0]))
				param.setResult(new UnsatisfiedLinkError());

		} else
			Util.log(this, Log.WARN, "Unknown method=" + param.method.getName());
	}

	@Override
	protected void after(XParam param) throws Throwable {
	}
}
