package io.oddworks.device.request;

import android.content.Context;
import io.oddworks.device.model.AuthToken;

/**
 * Class to initialize services in the io.oddworks.device.request package
 */
public class RestServiceProvider {
    private static RestServiceProvider instance;

    private ApiCaller apiCaller;
    private AuthenticationService authenticationService;

    private RestServiceProvider(Context context, String apiVersion, String accessToken) {
        OddParser.instance = new OddParser();
        RequestHandler.instance = new RequestHandler(context, apiVersion, accessToken);
        ApiCaller.instance = new ApiCaller(RequestHandler.instance, OddParser.instance);
        this.apiCaller = ApiCaller.instance;
        AuthenticationService.instance = new AuthenticationService(ApiCaller.instance, context);
        this. authenticationService = AuthenticationService.instance;
    }

    /** initialize services and this provider
     * @param apiVersion api version. e.g. "v1"*/
    public static void init(Context context, String apiVersion, String accessToken) {
        instance = new RestServiceProvider(context, apiVersion, accessToken );
    }

    /**
     * @return a RestServiceProvider instance if init has already been called. Otherwise returns null.
     */
    public static RestServiceProvider getInstance() {
        return RestServiceProvider.instance;
    }

    /** @throws NullPointerException if init has not already been called */
    public ApiCaller getApiCaller() {
        return instance.apiCaller;
    }

    /** @throws NullPointerException if init has not already been called */
    public AuthenticationService getAuthenticationService() {
        return instance.authenticationService;
    }
}