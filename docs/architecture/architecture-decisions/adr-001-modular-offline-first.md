# Modular Offline-First Architecture

Status: Accepted. Date: 2026-08-04.

## Context

Health data needs clear ownership, local availability, and low coupling.

## Decision

Use modular MVVM with Compose, Room as local source of truth, and feature-to-core dependency direction.

## Alternatives considered

A monolith or cloud-first architecture would increase coupling, network dependency, and privacy exposure.

## Consequences

More Gradle modules and explicit contracts are required, but testing and future feature work are safer.

