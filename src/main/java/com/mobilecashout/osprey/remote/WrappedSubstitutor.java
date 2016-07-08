/*
 * Copyright 2016 Innovative Mobile Solutions Limited and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilecashout.osprey.remote;

import com.mobilecashout.osprey.util.Substitutor;

import java.util.HashMap;

class WrappedSubstitutor extends Substitutor {
    private Substitutor substitutor;

    WrappedSubstitutor(HashMap<String, String> variables, Substitutor substitutor) {
        super(variables);
        this.substitutor = substitutor;
    }

    @Override
    public String replace(String text) {
        return super.replace(substitutor.replace(text));
    }
}
