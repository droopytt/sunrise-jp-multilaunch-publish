package com.malt.multilaunch.launcher;

import com.malt.multilaunch.Account;
import com.malt.multilaunch.login.RewrittenApiResponse;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewrittenLauncher extends Launcher<RewrittenApiResponse> {
    public static final String TTR_LOGIN_ENDPOINT = "https://www.toontownrewritten.com/api/login";

    public static final String EXECUTABLE_NAME = "TTREngine64.exe";

    @Override
    public Map<String, String> getEnvironmentVariables(RewrittenApiResponse response) {
        var map = new HashMap<String, String>(2);
        map.put("TTR_GAMESERVER", response.gameserver());
        map.put("TTR_PLAYCOOKIE", response.cookie());
        return map;
    }

    public RewrittenLauncher(Path workingDir, MultiControllerService multiControllerService) {
        super(workingDir, multiControllerService);
    }

    @Override
    protected Class<RewrittenApiResponse> responseType() {
        return RewrittenApiResponse.class;
    }

    @Override
    public URI getLoginApiUri() {
        return URI.create(TTR_LOGIN_ENDPOINT);
    }

    @Override
    public List<String> processArgs() {
        return List.of();
    }

    @Override
    public String executableName() {
        return EXECUTABLE_NAME;
    }

    @Override
    public void performPostLoginOverrides(List<Account> accounts, ActiveAccountManager activeAccountManager) {
        // TODO this can probably mostly just be reused in the superclass, copied over from the SunriseJpLauncher
    }
}
