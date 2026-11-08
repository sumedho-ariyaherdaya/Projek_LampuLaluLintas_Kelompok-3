# Four-Way Traffic Light Simulation

A beginner-friendly Java Swing starter project. Java2D draws a four-way road, lane and stop markings, and randomly appearing cars and motorcycles. Four colored straight-arrow signals are painted directly on the road at the approaches to the intersection. Each approach gets a one-second yellow before green, stays green for 20 seconds, then gets a one-second yellow before red. The next approach gets its one-second yellow before green; only one direction is green at a time. Vehicles are positioned in left-hand-traffic lanes. Left-turning vehicles may proceed on red, while straight and right-turning vehicles wait for green. Right turns are the most common route and follow north-to-west, east-to-north, south-to-east, and west-to-south. The stop line is behind the pavement arrow so waiting vehicles do not cover it. Vehicles appear a little more often, with a cap to keep the intersection readable. Click a vehicle to see its route and inspection results. Motorcycles use the three transparent PNG images in `src/main/resources/com/example/trafficlightsim/assets`: helmeted, no helmet, and noisy exhaust. No-helmet and noisy-exhaust images always have their matching violation; missing plates may apply to any motorcycle image or to a car. Cars only receive the missing-plate violation.
A supplied MP3 in the same assets folder plays on a loop. The Maven project uses MP3SPI to decode MP3 through Java Sound; NetBeans needs internet access once to download this dependency when it first loads the project.

## Open and run in NetBeans on Windows

1. Install a JDK 11 or newer if one is not already installed. NetBeans itself is not included.
2. Extract `TrafficLightSimulation.zip` if you downloaded the archive.
3. In NetBeans, choose **File → Open Project**.
4. Select the `TrafficLightSimulation` folder (the folder containing `pom.xml`) and click **Open Project**.
5. If NetBeans asks to resolve or reload the Maven project, accept.
6. In the Projects panel, expand **Source Packages → com.example.trafficlightsim**.
7. Right-click `Main.java` and choose **Run File**. You can also right-click the project and choose **Run**.

No external libraries are required.

## Project layout

```text
TrafficLightSimulation/
├── pom.xml
├── README.md
└── src/main/java/com/example/trafficlightsim/
    ├── Main.java
    ├── SimulationPanel.java
    └── Vehicle.java
```

`Main` creates the maximized window. `SimulationPanel` handles drawing, the signal cycle, spawning, animation, and vehicle clicks. `Vehicle` stores each vehicle's direction, type, position, color, and inspection results.

## Adding dashboard controls later

`Main.java` places the simulation panel at the upper-left and sizes it to about 70% of the screen width and 60% of its height. The surrounding window area is intentionally left open. Replace the simple `FlowLayout` content panel with a `BorderLayout` or `GridBagLayout` when adding counters, buttons, or status panels. Adjust `canvasWidth` and `canvasHeight` in `Main.java` to change the simulation footprint.
