/*
 * Copyright 2024 NetKnights GmbH - lukas.matusiewicz@netknights.it
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.privacyidea.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.session.context.navigate.CanonicalUsernameLookupStrategy;
import org.jetbrains.annotations.NotNull;
import org.opensaml.profile.context.ProfileRequestContext;
import org.privacyidea.context.Config;
import org.privacyidea.context.PIContext;
import org.privacyidea.context.PIFormContext;
import org.privacyidea.context.PIServerConfigContext;
import org.privacyidea.context.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class InitializePIContext extends AbstractAuthenticationAction
{
    private static final Logger log = LoggerFactory.getLogger(InitializePIContext.class);
    @Nonnull
    private final CanonicalUsernameLookupStrategy usernameLookupStrategy;
    private String serverURL;
    @Nullable
    private String realm;
    private boolean verifySSL;
    @Nullable
    private String defaultMessage;
    private String authenticationFlow;
    @Nullable
    private String serviceName;
    @Nullable
    private String servicePass;
    @Nullable
    private String serviceRealm;
    @Nullable
    private String staticPass;
    @Nullable
    private String forwardHeaders;
    @Nullable
    private String otpFieldHint;
    @Nullable
    private String otpLength;
    @Nullable
    private String pluginVersion;
    @Nullable
    private String pollingInterval;
    private boolean pollInBrowser;
    @Nullable
    private String pollInBrowserUrl;
    private boolean debug;

    public InitializePIContext()
    {
        usernameLookupStrategy = new CanonicalUsernameLookupStrategy();
    }

    @Override
    protected void doExecute(@NotNull ProfileRequestContext profileRequestContext, @NotNull AuthenticationContext authenticationContext)
    {
        authenticationContext.removeSubcontext(PIContext.class);
        authenticationContext.removeSubcontext(PIServerConfigContext.class);

        User user = getUser(profileRequestContext);
        if (user == null)
        {
            log.info("{} No principal name available.", getLogPrefix());
        }
        else
        {
            PIServerConfigContext piServerConfigContext = getConfigBaseContext();
            log.info("{} Create PIServerConfigContext {}", this.getLogPrefix(), piServerConfigContext);
            authenticationContext.addSubcontext(piServerConfigContext);

            PIContext piContext = new PIContext(user, pluginVersion);
            log.info("{} Create PIContext {}", this.getLogPrefix(), piContext);
            authenticationContext.addSubcontext(piContext);

            PIFormContext piFormContext = new PIFormContext(defaultMessage, otpFieldHint, getOtpLength(), pollingInterval, pollInBrowser, pollInBrowserUrl);
            log.info("{} Create PIFormContext {}", this.getLogPrefix(), piFormContext);
            authenticationContext.addSubcontext(piFormContext);
        }
    }

    @Nullable
    private User getUser(@Nonnull ProfileRequestContext profileRequestContext)
    {
        String collectedUser = usernameLookupStrategy.apply(profileRequestContext);
        if (!StringUtils.hasText(collectedUser))
        {
            return null;
        }
        else
        {
            return new User(collectedUser);
        }
    }

    @NotNull
    private PIServerConfigContext getConfigBaseContext()
    {
        String authenticationFlow;
        String staticPass = null;
        if (this.authenticationFlow.equals("triggerChallenge"))
        {
            authenticationFlow = "triggerChallenge";
        }
        else if (this.authenticationFlow.equals("sendStaticPass"))
        {
            authenticationFlow = "sendStaticPass";
            if (this.staticPass != null)
            {
                staticPass = this.staticPass;
            }
        }
        else
        {
            authenticationFlow = "default";
        }
        Config configParams = new Config(serverURL, realm, verifySSL, authenticationFlow, serviceName, servicePass, serviceRealm, staticPass, pollInBrowser, forwardHeaders, otpLength, debug);
        return new PIServerConfigContext(configParams);
    }

    @Nullable
    private Integer getOtpLength()
    {
        if (otpLength != null)
        {
            try
            {
                return Integer.parseInt(otpLength);
            }
            catch (NumberFormatException e)
            {
                log.info("{} Config option \"otp_length\": Wrong format. Only digits allowed.", getLogPrefix());
            }
        }
        return null;
    }

    // Spring bean property setters
    public void setServerURL(@Nonnull String serverURL) {this.serverURL = serverURL;}

    public void setRealm(@Nullable String realm) {this.realm = realm;}

    public void setVerifySSL(boolean verifySSL) {this.verifySSL = verifySSL;}

    public void setDefaultMessage(@Nullable String defaultMessage) {this.defaultMessage = defaultMessage;}

    public void setOtpFieldHint(@Nullable String otpFieldHint) {this.otpFieldHint = otpFieldHint;}

    public void setAuthenticationFlow(@Nonnull String authenticationFlow) {this.authenticationFlow = authenticationFlow;}

    public void setServiceName(@Nullable String serviceName) {this.serviceName = serviceName;}

    public void setServicePass(@Nullable String servicePass) {this.servicePass = servicePass;}

    public void setServiceRealm(@Nullable String serviceRealm) {this.serviceRealm = serviceRealm;}

    public void setStaticPass(@Nullable String staticPass) {this.staticPass = staticPass;}

    public void setForwardHeaders(@Nullable String forwardHeaders) {this.forwardHeaders = forwardHeaders;}

    public void setOtpLength(@Nullable String otpLength) {this.otpLength = otpLength;}

    public void setPollingInterval(@Nullable String pollingInterval) {this.pollingInterval = pollingInterval;}

    public void setPollInBrowser(boolean pollInBrowser) {this.pollInBrowser = pollInBrowser;}

    public void setPollInBrowserUrl(@Nullable String pollInBrowserUrl) {this.pollInBrowserUrl = pollInBrowserUrl;}

    public void setPluginVersion(@Nullable String pluginVersion) {this.pluginVersion = pluginVersion;}

    public void setDebug(boolean debug) {this.debug = debug;}
}