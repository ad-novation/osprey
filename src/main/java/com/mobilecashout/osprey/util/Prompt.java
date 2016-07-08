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

package com.mobilecashout.osprey.util;

import java.util.Scanner;

public class Prompt {
    public static boolean confirm(String text) {
        System.out.println(text);
        System.out.print("Y/N > ");
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.next();
        return userInput.equalsIgnoreCase("y") || userInput.equalsIgnoreCase("yes");
    }

    public static void pause(String text) {
        System.out.println(text);
        System.out.print("> ");
        Scanner input = new Scanner(System.in);
        String readString = input.nextLine();
        while (readString != null) {
            if (readString.equals("")) {
                return;
            }
            if (input.hasNextLine()) {
                readString = input.nextLine();
            } else {
                readString = null;
            }
        }
    }
}
