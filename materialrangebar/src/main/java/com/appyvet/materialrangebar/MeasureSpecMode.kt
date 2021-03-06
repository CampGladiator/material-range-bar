/*
 * Copyright 2013, Edmodo, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.appyvet.materialrangebar

import android.view.View

/**
 * Helper enum class for transforming a measureSpec mode integer value into a
 * human-readable String. The human-readable String is simply the name of the
 * enum value.
 */
enum class MeasureSpecMode // Constructor /////////////////////////////////////////////////////////////
(
        /**
         * Gets the int value associated with this mode.
         *
         * @return the int value associated with this mode
         */
        // Member Variables ////////////////////////////////////////////////////////
        val modeValue: Int) {
    AT_MOST(View.MeasureSpec.AT_MOST), EXACTLY(View.MeasureSpec.EXACTLY), UNSPECIFIED(View.MeasureSpec.UNSPECIFIED);

    // Public Methods //////////////////////////////////////////////////////////

    companion object {
        /**
         * Gets the MeasureSpecMode value that corresponds with the given
         * measureSpec int value.
         *
         * @param measureSpec the measure specification passed by the platform to
         * [android.view.View.onMeasure]
         * @return the MeasureSpecMode that matches with that measure spec
         */
        fun getMode(measureSpec: Int): MeasureSpecMode? {
            val modeValue = View.MeasureSpec.getMode(measureSpec)
            for (mode in values()) {
                if (mode.modeValue == modeValue) {
                    return mode
                }
            }
            return null
        }
    }

}