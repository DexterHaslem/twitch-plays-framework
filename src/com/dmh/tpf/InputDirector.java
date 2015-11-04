/*
twitch-plays-framework Copyright 2015 Dexter Haslem <dexter.haslem@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.dmh.tpf;

public abstract class InputDirector {
    protected String processName;
    public abstract boolean sendKey(int keyCode);
    //public abstract boolean sendMouse(int x, int y, boolean isDown);
    public abstract boolean findInstance();

    protected InputDirector(String processName) {
        this.processName = processName;
    }
}
