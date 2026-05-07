# neon

An entity-component-system framework for Java, in the Artemis / Ashley lineage. Aspects, systems, entity managers, finite state machines, an event bus, and a minimal `@Wire` dependency-injection mechanism.

## Why it exists

Artemis was the reference ECS for Java for a long time. Ashley took its place as part of libGDX. `neon` is a third path: a smaller runtime that strips both down to what a small game actually needs and adds a lighter DI model.

## Architecture

Core concepts:

- **`Entity`** — an opaque integer id with a bag of components.
- **`Component`** — pure data. No logic.
- **`Aspect`** — a set-based filter over components: `{all: [Position, Velocity], none: [Frozen]}`.
- **`EntitySystem`** — iterates entities matching an aspect, each tick.
- **`EntityManager`** / **`ComponentManager`** — storage, lifecycle, lookup.
- **`EventBus`** — typed pub/sub for cross-system signalling.
- **`@Wire`** — field-level DI resolved by the world container. Avoids constructor bloat.
- **`FiniteStateMachine`** — generic FSM helper usable at entity, component, or system level.

Design goals: zero-allocation hot paths, bitset-based aspect matching, deterministic iteration order.

## Build

```bash
mvn clean install
```

JDK 8 or later.

## Minimal example

```java
World world = new WorldBuilder()
    .with(new MovementSystem())
    .with(new RenderSystem())
    .build();

int e = world.createEntity();
world.edit(e).add(new Position(0, 0)).add(new Velocity(1, 0));

while (running) {
    world.process(deltaTime);
}
```

A system:

```java
public class MovementSystem extends EntityProcessingSystem {
    @Wire ComponentMapper<Position> pos;
    @Wire ComponentMapper<Velocity> vel;

    public MovementSystem() {
        super(Aspect.all(Position.class, Velocity.class));
    }

    @Override
    protected void process(int entityId) {
        Position p = pos.get(entityId);
        Velocity v = vel.get(entityId);
        p.x += v.dx;
        p.y += v.dy;
    }
}
```

## What it doesn't do

- No built-in rendering, physics, audio, input. Pure ECS runtime.
- No serialization. Plug your own.
- No multi-threaded system scheduling. One thread, one world, one tick loop.
- No component archetypes (no struct-of-arrays storage). Classic component-per-entity map.

## License

MIT.
