# Plants Module Checkstyle Issues - TODO List

## Summary
- **Total violations**: 146 warnings
- **Main source violations**: 6
- **Test source violations**: 140
- **Files affected**: 4

## 📋 TODO by Class

### 🏗️ Main Source Files

#### Plant.java
- [ ] Make 1 variable final (line 65)

#### PlantController.java
- [ ] Make 5 variables final (lines 72, 119, 120, 151, 163)

### 🧪 Test Source Files

#### PlantControllerTest.java
- [ ] Rename 7 test methods to follow camelCase naming convention
- [ ] Make 33 variables final throughout the test methods

#### PlantServiceTest.java
- [ ] Rename 1 test method to follow camelCase naming convention
- [ ] Make 47 variables final throughout the test methods
- [ ] Fix variable declaration usage distance issue (1 case where distance is 9, max allowed is 3)

## 🎯 Priority Groups

### High Priority (Method Names)
- [ ] Fix 8 method names across test classes to follow proper camelCase naming

### Medium Priority (Final Variables)
- [ ] Add final modifier to 86 variables across all classes

### Low Priority (Code Style)
- [ ] Fix 1 variable declaration usage distance issue in PlantServiceTest

## 📊 Statistics
- **Total files to modify**: 4
- **Total issues to fix**: 146
- **Method naming issues**: 8
- **Final variable issues**: 86
- **Variable declaration distance issues**: 1
- **Other issues**: 1