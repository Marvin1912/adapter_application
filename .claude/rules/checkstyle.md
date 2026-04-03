---
description: Checkstyle rules to follow when writing or modifying Java code
globs: **/*.java
---

# Checkstyle Guidelines

All Java code must pass checkstyle with zero warnings. The full config is in `config/checkstyle/checkstyle.xml`.

## Formatting

- Max line length: 160 characters
- Use standard indentation (4 spaces, no tabs)
- K&R brace style (`LeftCurly`, `RightCurly`)
- One statement per line
- No extra whitespace before/after parens or typecasts

## Naming

- Follow standard Java naming conventions for constants, variables, members, methods, packages, parameters, static variables, and types
- Lambda parameters, pattern variables, and record components follow the same conventions

## Imports

- No wildcard imports (`AvoidStarImport`)
- No unused or redundant imports
- No illegal imports

## Methods & Parameters

- Max method length: 100 lines
- Max parameters: 7
- Max anonymous inner class length: default (20 lines)

## Nesting Limits

- Max nested `for` depth: 2
- Max nested `if` depth: 3
- Max nested `try` depth: 2

## Coding Rules

- No `System.out.println` — use a logger
- No `java.util.Optional` as a method parameter
- `final` local variables where possible (`FinalLocalVariable`)
- `default` case must come last in switch statements and must always be present
- No fall-through in switch cases without explicit comment
- No inner assignments, no modified control variables
- Use `equals()` not `==` for String comparison (`StringLiteralEquality`)
- Simplify boolean expressions and returns
- Declare variables close to first use (`VariableDeclarationUsageDistance`)
- One variable declaration per line (`MultipleVariableDeclarations`)
- No unnecessary parentheses
- Follow standard declaration order (static vars, instance vars, constructors, methods)
- Implement `hashCode()` when overriding `equals()` (`EqualsHashCode`)

## Documentation

- Javadoc required on public methods (`JavadocMethod`) and types (`JavadocType`)
- Single-line Javadoc where appropriate
- Valid Javadoc positioning

## Hidden Fields

- Constructor parameters and setters may shadow field names (Lombok-friendly)
