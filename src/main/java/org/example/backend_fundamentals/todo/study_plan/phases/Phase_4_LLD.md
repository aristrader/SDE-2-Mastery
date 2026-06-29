# Low-Level Design (LLD) Interview Curriculum (SDE-2 Backend)

> Goal: Learn OOP, SOLID, and Design Patterns organically through machine coding problems. Focus on interview coverage, not problem count.

## 1. Foundation (1–2 Days)
#### OOP Fundamentals
- [ ] Abstraction
- [ ] Encapsulation
- [ ] Inheritance
- [ ] Polymorphism
- [ ] Object-Oriented Design
- [ ] Interfaces vs Abstract Classes
- [ ] Composition over Inheritance
- [ ] Association / Aggregation / Composition
- [ ] Dependency Injection
- [ ] Immutability
- [ ] Domain Modeling
#### SOLID Principles
- [ ] SRP
- [ ] OCP
- [ ] LSP
- [ ] ISP
- [ ] DIP
#### UML
- [ ] Basic Class Diagrams
- [ ] Relationships
#### Clean Code
- [ ] Separation of Concerns
- [ ] Layered Architecture
- [ ] Error Handling
- [ ] Testability
#### Interview Design Process
- [ ] Requirement clarification
- [ ] Identifying entities and responsibilities
- [ ] Designing APIs before implementation
- [ ] Choosing appropriate abstractions and interfaces
## 2. Core Interview Problems (Recommended Order)
### 1. Parking Lot ⭐⭐⭐⭐⭐

#### Variants

- [ ] Multi-level Parking Lot
- [ ] Smart Parking

#### Design Patterns

- [ ] Strategy
- [ ] Factory
- [ ] Singleton (optional)

#### Concepts

- [ ] SOLID
- [ ] Composition
- [ ] Polymorphism
- [ ] Extensibility
- [ ] Domain Modeling
### 2. Vending Machine ⭐⭐⭐⭐⭐

#### Variants

- [ ] Coffee Machine
- [ ] ATM (basic)
- [ ] Ticket Machine

#### Design Patterns

- [ ] State
- [ ] Strategy

#### Concepts

- [ ] State Machines
- [ ] Business Rules
- [ ] Encapsulation
- [ ] Open/Closed Design
### 3. Splitwise ⭐⭐⭐⭐⭐

#### Variants

- [ ] Expense Sharing
- [ ] Bill Splitting

#### Design Patterns

- [ ] Strategy

#### Concepts

- [ ] Domain Modeling
- [ ] Validation
- [ ] Separation of Responsibilities
- [ ] Extensible Algorithms
4. BookMyShow / Seat Booking ⭐⭐⭐⭐⭐

#### Variants

- [ ] Event Booking
- [ ] Flight Seat Booking
- [ ] Stadium Seat Booking

#### Design Patterns

- [ ] Observer
- [ ] Factory

#### Concepts

- [ ] Reservation Workflow
- [ ] Concurrency
- [ ] Locking
- [ ] Object Collaboration
- [ ] Extensibility
5. Elevator System ⭐⭐⭐⭐⭐

#### Variants

- [ ] Multi Elevator
- [ ] Lift Scheduler

#### Design Patterns

- [ ] State
- [ ] Strategy

#### Concepts

- [ ] Scheduling
- [ ] State Machines
- [ ] Object Collaboration
- [ ] Extensible Policies
6. Chess ⭐⭐⭐⭐☆

#### Variants

- [ ] Tic Tac Toe
- [ ] Snake Game
- [ ] Ludo (simplified)

#### Design Patterns

- [ ] State
- [ ] Strategy
- [ ] Factory

#### Concepts

- [ ] Rich Domain Modeling
- [ ] Rules Engine
- [ ] Polymorphism
- [ ] Encapsulation
7. Logger ⭐⭐⭐⭐☆

#### Variants

- [ ] Logging Framework
- [ ] Log4j-like Design

#### Design Patterns

- [ ] Chain of Responsibility
- [ ] Factory

#### Concepts

- [ ] Extensible Pipelines
- [ ] Configurable Processing
- [ ] Open/Closed Principle
8. Rate Limiter ⭐⭐⭐⭐⭐

#### Variants

- [ ] Token Bucket
- [ ] Leaky Bucket
- [ ] Sliding Window
- [ ] Fixed Window

#### Design Patterns

- [ ] Strategy
- [ ] Factory

#### Concepts

- [ ] Concurrency
- [ ] Thread Safety
- [ ] Dependency Injection
- [ ] Interface-driven Design
- [ ] Extensibility
9. File Search (Unix Find) ⭐⭐⭐⭐☆

#### Variants

- [ ] File Filter
- [ ] Search API
- [ ] Rule Engine

#### Design Patterns

- [ ] Composite
- [ ] Specification

#### Concepts

- [ ] Recursive Structures
- [ ] Predicate Composition
- [ ] Extensible Filtering
10. Ordering System ⭐⭐⭐⭐☆

#### Variants

- [ ] Swiggy
- [ ] Zomato
- [ ] Restaurant
- [ ] Grocery Ordering

#### Design Patterns

- [ ] Observer
- [ ] Strategy
- [ ] Factory

#### Concepts

- [ ] Order Lifecycle
- [ ] Composition
- [ ] Object Collaboration
- [ ] Extensibility
11. Library Management ⭐⭐⭐⭐☆

#### Variants

- [ ] Inventory Management
- [ ] Rental System

#### Design Patterns

- [ ] Factory

#### Concepts

- [ ] CRUD Domain Modeling
- [ ] Entity Relationships
- [ ] Validation
- [ ] Layer Separation
12. Locker Management ⭐⭐⭐⭐☆

#### Variants

- [ ] Amazon Locker
- [ ] Package Pickup

#### Design Patterns

- [ ] Strategy
- [ ] Factory

#### Concepts

- [ ] Allocation Policies
- [ ] Dependency Injection
- [ ] Extensibility
13. Generic Cache (LRU/LFU) ⭐⭐⭐⭐☆

#### Variants

- [ ] In-memory Cache
- [ ] Generic Cache

#### Design Patterns

- [ ] Strategy

#### Concepts

- [ ] Generic Design
- [ ] Interface-driven Design
- [ ] Separation of Storage & Eviction Policy

> Focus on object-oriented design only, not distributed caching.

## 3. Additional Practice (Only If Time Permits)
- [ ] Car Rental System
- [ ] Digital Wallet
- [ ] Employee Management System
- [ ] Meeting Scheduler
- [ ] Task Management System
- [ ] Job Scheduler
## Design Patterns to Learn Organically
#### Must Know
- [ ] Strategy
- [ ] Factory
- [ ] State
- [ ] Observer
- [ ] Chain of Responsibility
- [ ] Composite
- [ ] Specification
#### Nice to Know
- [ ] Builder
- [ ] Decorator
- [ ] Command
- [ ] Adapter
- [ ] Template Method
- [ ] Proxy

> Do not study the GoF patterns independently. Learn them through the interview problems above.

## Final Learning Order
- [ ] Foundation
- [ ] Parking Lot
- [ ] Vending Machine
- [ ] Splitwise
- [ ] BookMyShow / Seat Booking
- [ ] Elevator System
- [ ] Chess
- [ ] Logger
- [ ] Rate Limiter
- [ ] File Search (Unix Find)
- [ ] Ordering System
- [ ] Library Management (includes Inventory & Rental variants)
- [ ] Locker Management
- [ ] Generic Cache (LRU/LFU)
- [ ] Additional Practice (if time remains)
