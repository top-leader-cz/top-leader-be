package com.topleader.topleader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.test.context.aot.DisabledInAotMode;

/**
 * Test validating the modular structure of the application.
 * Spring Modulith ensures that modules are properly isolated and dependencies are correct.
 *
 * All modules are declared as Type.OPEN via package-info.java, which:
 * - Exposes all sub-package types to other modules
 * - Enables cycle detection across all inter-module dependencies
 *
 * Run via: ./gradlew modularityCheck
 */
@Disabled
@DisabledInAotMode
class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(TopLeaderApplication.class);

    @Test
    void verifiesModularStructure() {
        // This will verify:
        // ✅ All modules can use 'common' (it's Type.OPEN)
        // ❌ 'common' should NOT depend on business modules (coach, user, session, etc.)
        // ❌ No cyclic dependencies between modules
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules)
                .writeDocumentation()
                .writeModulesAsPlantUml();
    }
}
