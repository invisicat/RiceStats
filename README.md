# RiceStats
I made this plugin for my SMP server. It's able to track statistics and send it to InfluxDB where it can be processed by an analytics program such as Grafana. Don't
flame me for my inexperience with InfluxDB :p, I've only used it a couple times.

## Table of Contents
- [RiceStats](#ricestats)
  * [Installation](#installation)
  * [Tech Stack](#tech-stack)
  * [Tracked Statistics](#tracked-statistics)
    + [Server Statistics](#server-statistics)
    + [Player Statistics](#player-statistics)
  * [Examples](#examples)
- [InfluxDB Technicalities](#influxdb-technicalities)

## Installation

Make sure you install Paper 1.18.1 or a similar paper-fork (Please use 1.18.1 because I am using NMS fields from that version). Once you do you can either 
deploy InfluxDB through the provided `docker-compose.yml` by 
running:
```bash
  docker-compose up -d
```

> Alternatively, you can deploy your own InfluxDB 1.0 instance.

Once you've deployed your InfluxDB Database you'll have to fill out the authentication parameters in the plugin config.

Launch the server and you'll start to see statistics flowing into InfluxDB!

## Tech Stack

The current technologies that the plugin is currently using
- [Paper](https://papermc.io/) - The server api that this plugin is using to interact with Spigot.
- [InfluxDB](https://www.influxdata.com/) - A realtime time series database to track and store statistics that are brought in.
- [Spigot](https://www.spigotmc.org/) - The Minecraft server software that runs everything.
- [Grafana](https://grafana.com/) - The visualization tool that I use to visualize and analyze the statistics.

## Tracked Statistics

### Server Statistics

- [x] TPS (1min,5min,15min)
- [x] Average MSPT
- [x] Available CPU Cores
- [x] CPU Usage 10s, 1min, 15m, Realtime)
- [x] Total Memory
- [x] Free Memory
- [x] Max Memory
- [x] Current players
- [x] Max Players

### Player Statistics
- [x] Blocks Broken
- [x] Blocks placed
- [x] Animals bred
- [x] Sheeps dyed
- [x] Entities leashed
- [x] Fish caught
- [x] Entities mounted
- [x] Monsters killed
- [x] Experience Earned
- [x] Deaths
- [x] Items crafted
- [x] Foods eaten
- [x] Time Ingame
- [x] Times joined
- [x] Times left
- [x] World changing (Going to the nether/end)
- [x] Villager trading
- [x] Raids finished
- [x] Raids failed
- [x] Raid waves completed
- [x] Raids started


## Examples
Here is an example of how I visualized the data from my SMP in Grafana using this plugin.

> Per Player Statistics with player swapping variables
![Image](https://imgur.com/iSY1KVU.png)

> Server statistics with some pie graphs and MSPT alerts
![Image2](https://imgur.com/OyLJycv.png)


If you would like to use or check out this visualizer. You can check it out [here](https://snapshot.raintank.io/dashboard/snapshot/5Yo7prny81BNLfwU38zkRv2ZKH0mM4Ik)

# InfluxDB Technicalities
All player related points have a `player` and a `uuid` tag attached to them which are the player's username and uuid. You can use this to filter by player.
On top of that, trackers that track anything that tracks any static value in Bukkit such as Materials are also stored as tags for easy access. Below,
I have provided a list of each point and what they do. If you're too lazy or if I don't finish the list. You're able to look into the source code at
`trackers/<category>/<Tracker>` (or click [me](https://github.com/RiceCX/RiceStats/tree/master/src/main/java/cc/ricecx/ricestats/trackers) and see what trackers are available + the tags and fields that they provide.


# Contributions
Contributions are welcome! Feel free to make a pull request if you feel that something can be done better
or you want to add in a new or improve a feature.

# License
The MIT License (MIT)

Copyright (c) 2022 RiceCX

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
