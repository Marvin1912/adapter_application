# Dependency Cleanup Plan

## Overview
This plan outlines the steps to remove unused dependencies from the Gradle build configuration based on analysis of the root `build.gradle` and module usage. The goal is to optimize build times, reduce classpath size, and eliminate unnecessary dependencies.

## Identified Unused Dependencies
1. **spring-boot-starter-webflux** and **spring-boot-starter-security** in `camt`, `database`, and `consul` modules
   - These modules do not contain web controllers, reactive endpoints, or security configurations.
   - Only the `api` module uses these dependencies appropriately.

2. **org.mapstruct:mapstruct** and **org.mapstruct:mapstruct-processor** across all modules
   - No `@Mapper` annotations found in existing modules.
   - If MapStruct is planned for future modules, it can be added conditionally.

## Proposed Changes

### 1. Update Root `build.gradle`
- Modify the dependency inclusion logic to exclude `camt`, `database`, and `consul` from receiving `spring-boot-starter-webflux` and `spring-boot-starter-security`.
- Remove `org.mapstruct:mapstruct` and `org.mapstruct:mapstruct-processor` from the common dependencies block.

**Before:**
```gradle
if (name in ['api', 'importer', 'camt', 'database', 'consul', 'plants', 'image-server', 'vocabulary', 'mental-arithmetic']) {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
```

**After:**
```gradle
if (name in ['api']) {  // Only include for modules that actually use webflux/security
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
```

Remove MapStruct:
```gradle
// Remove these lines from the common dependencies
implementation 'org.mapstruct:mapstruct:1.6.3'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
```

### 2. Handle Reactor Usage in `camt`
- The `camt` module uses `Flux` and `Mono` from Project Reactor for data processing.
- If reactive streams are still needed, add `io.projectreactor:reactor-core` explicitly to `camt/build.gradle`.

**Add to `camt/build.gradle`:**
```gradle
dependencies {
    // ... existing
    implementation 'io.projectreactor:reactor-core'
}
```

### 3. Move `commons-lang3` to Specific Modules (Optional Optimization)
- Currently used only in `consul`.
- To further optimize, remove from root and add to `consul/build.gradle`.

**Remove from root:**
```gradle
// Remove this line
implementation 'org.apache.commons:commons-lang3'
```

**Add to `consul/build.gradle`:**
```gradle
dependencies {
    // ... existing
    implementation 'org.apache.commons:commons-lang3'
}
```

### 4. Clean Up Non-Existent Modules
- The root `build.gradle` references modules like `importer`, `plants`, `image-server`, `vocabulary`, `mental-arithmetic`, `influxdb`, `uploader` that do not exist in the current project structure.
- Update the conditional blocks to only include existing modules or remove unused conditions.

## Implementation Steps
1. **Backup current `build.gradle`** and module `build.gradle` files.
2. **Apply changes to root `build.gradle`** as described.
3. **Update `camt/build.gradle`** to include Reactor Core if needed.
4. **Update `consul/build.gradle`** to include commons-lang3 if moving it.
5. **Run `./gradlew build`** to verify no compilation errors.
6. **Test the application** to ensure functionality remains intact.
7. **Review build time improvements** and classpath size reduction.

## Risks and Considerations
- Ensure that removing dependencies doesn't break transitive dependencies.
- If MapStruct is intended for future development, document the decision to exclude it.
- Monitor for any runtime issues related to removed dependencies.
- Consider using Gradle's dependency analysis plugins (e.g., `gradle-dependency-analyze`) for future audits.

## Expected Benefits
- Faster builds due to smaller dependency trees.
- Reduced JAR sizes and memory usage.
- Cleaner build configuration aligned with actual usage.</content>
</xai:function_call">The dependency cleanup plan has been created and saved to `plans/dependency_cleanup_plan.md`. This plan provides a structured approach to removing unused dependencies, with step-by-step instructions and considerations for implementation.