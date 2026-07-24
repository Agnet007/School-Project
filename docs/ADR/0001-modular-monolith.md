# ADR 0001: Modular monolith

Status: Accepted.

Use one Spring Boot deployment with package-by-feature boundaries. Cross-feature access occurs through application contracts or domain events, not repositories. This avoids distributed operations while retaining separable ownership.