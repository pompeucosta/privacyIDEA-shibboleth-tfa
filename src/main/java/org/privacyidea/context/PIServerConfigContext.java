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
package org.privacyidea.context;

import javax.annotation.Nonnull;
import org.opensaml.messaging.context.BaseContext;

public class PIServerConfigContext extends BaseContext
{
    @Nonnull
    Config configParams;

    public PIServerConfigContext(@Nonnull Config configParams)
    {
        this.configParams = configParams;
    }

    @Nonnull
    public Config getConfigParams()
    {
        return configParams;
    }
}