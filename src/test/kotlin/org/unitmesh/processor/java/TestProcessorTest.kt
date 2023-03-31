package org.unitmesh.processor.java

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestProcessorTest {
    @Test
    fun `should remove license info before import`() {
        val code = """
            /*
             * Licensed to the Apache Software Foundation (ASF) under one
             * or more contributor license agreements.  See the NOTICE file
             * distributed with this work for additional information
             * regarding copyright ownership.  The ASF licenses this file
             * to you under the Apache License, Version 2.0 (the
             * "License"); you may not use this file except in compliance
             * with the License.  You may obtain a copy of the License at
             *
             *     http://www.apache.org/licenses/LICENSE-2.0
             *
             * Unless required by applicable law or agreed to in writing, software
             * distributed under the License is distributed on an "AS IS" BASIS,
             * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
             * See the License for the specific language governing permissions and
             * limitations under the License.
             */
            package com.thoughtworks.go.server.messaging.scheduling;

            import com.thoughtworks.go.domain.AgentRuntimeStatus;
            import com.thoughtworks.go.helper.AgentMother;
            
            class TestProcessorTest {
            }
         """.trimIndent()

        val processor = TestProcessor(code)
        processor.removeLicenseInfoBeforeImport()
        val output = processor.output()
        output shouldBe """
            package com.thoughtworks.go.server.messaging.scheduling;

            import com.thoughtworks.go.domain.AgentRuntimeStatus;
            import com.thoughtworks.go.helper.AgentMother;
            
            class TestProcessorTest {
            }

         """.trimIndent()
    }
}