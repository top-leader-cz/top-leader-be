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
 * Note: The 'common' module is a SHARED KERNEL (Type.OPEN) - all modules can depend on it.
 * However, 'common' itself should NOT depend on any business modules.
 */
@DisabledInAotMode
@Disabled
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
