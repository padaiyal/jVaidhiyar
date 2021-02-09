<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
<div align="center">
  <h1 align="center">jVaidhiyar</h1>
  <p align="center">
    A library for retrieving JVM related resource usage and configuration information.
    <br />
    <a href="https://github.com/padaiyal/jVaidhiyar/issues/new/choose">Report Bug/Request Feature</a>
  </p>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![Apache License][license-shield]][license-url] <br>
![Maven build - Ubuntu latest](https://github.com/padaiyal/jVaidhiyar/workflows/Maven%20build%20-%20Ubuntu%20latest/badge.svg?branch=main)
![Maven build - Windows latest](https://github.com/padaiyal/jVaidhiyar/workflows/Maven%20build%20-%20Windows%20latest/badge.svg?branch=main)
![Maven build - MacOS latest](https://github.com/padaiyal/jVaidhiyar/workflows/Maven%20build%20-%20MacOS%20latest/badge.svg?branch=main)
![Publish to GitHub packages](https://github.com/padaiyal/jVaidhiyar/workflows/Publish%20to%20GitHub%20packages/badge.svg)
</div>

<!--
*** To avoid retyping too much info. Do a search and replace with your text editor for the following:
    'jVaidhiyar'
 -->

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
        <a href="#usage">Usage</a>
    </li>
    <li>
        <a href="#roadmap">Roadmap</a>
    </li>
    <li>
        <a href="#contributing">Contributing</a>
    </li>
    <li>
        <a href="#license">License</a>
    </li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## About The Project
This library retrieves the following JVM related information:
 - Thread Information (Name, Priority, ID, Stack, CPU usage etc)
 - Heap usage information
 - Non heap usage information
 - VM options
 - Heap dump

<!-- USAGE -->
## Usage
This project is to be used as a dependency to other projects.
Adding this project as a dependency is as follows:
 1. Download the latest jar for this project from [GitHub packages](https://github.com/orgs/padaiyal/packages?repo_name=jVaidhiyar) and place it within 
    the dependant project.
 2. Add the following dependency tag to the pom.xml of the dependant project:
    ```
    <dependency>
        <groupId>org.java.padaiyal.utilities</groupId>
        <artifactId>vaidhiyar</artifactId>
        <version>2021.02.09</version>
        <scope>system</scope>
        <systemPath>${basedir}/<PATH_TO_JAR></systemPath>
    </dependency>
    ```
    NOTE: Refer the [GitHub packages](https://github.com/orgs/padaiyal/packages?repo_name=jVaidhiyar) 
    / [releases](https://github.com/padaiyal/jVaidhiyar/releases) section for this repo to know 
    the latest released version of this project.

Here's a sample snippet showing the usage of JvmUtility:
```
// Get the heap or non heap memory info.
ExtendedMemoryUsage extendedMemoryUsage = JvmUtility.getHeapMemoryUsage(); // Or JvmUtility.getNonHeapMemoryUsage()
double memoryUsagePercentage = extendedMemoryUsage.getMemoryUsagePercentage();
long initMemoryInBytes = extendedMemoryUsage.getInit();
long committedMemoryInBytes = extendedMemoryUsage.getCommitted();
long maxMemoryInBytes = extendedMemoryUsage.getMax();

// Get the configured VM options.
JsonArray vmOptions = JvmUtility.getAllVmOptions();

// Information on the most recent garbage collection.
GarbageCollectionInfo[] gcInfos = JvmUtility.getGarbageCollectionInfo();

// IDs of all threads running in the JVM.
long[] threadIds = JvmUtility.getAllThreadsId();

// Get information on JVM threads.
int threadStackDepth = 10;
ExtendedThreadInfo[] extendedThreadInfos = JvmUtility.getAllExtendedThreadInfo(threadStackDepth);
double threadCpuUsage = extendedThreadInfos[0].getCpuUsage();
ThreadInfo threadInfo = extendedThreadInfos[0].getThreadInfo();
long threadMemoryAllocatedInBytes = extendedThreadInfos[0].getMemoryAllocatedInBytes();

// Generate a heap dump.
Path destinationDirectory = ...;
String heapDumpFileName = "heapDump.hprof";
boolean dumpOnlyLiveObjects = false;
JvmUtility.generateHeapDump(
    destinationDirectory,
    heapDumpFileName,
    dumpOnlyLiveObjects
);
...
```
For more such examples, checkout [JvmUtilityTest](https://github.com/padaiyal/jVaidhiyar/tree/main/src/test/java/org/padaiyal/utilities/vaidhiyar/JvmUtilityTest.java)

<!-- ROADMAP -->
## Roadmap
See the [open issues](https://github.com/padaiyal/jVaidhiyar/issues) for a list of proposed features (and known issues).

<!-- CONTRIBUTING -->
## Contributing
Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project.
2. Create your branch. (`git checkout -b contribution/AmazingContribution`)
3. Commit your changes. (`git commit -m 'Add some AmazingContribution'`)
4. Push to the branch. (`git push origin contribution/AmazingContribution`)
5. Open a Pull Request.


<!-- LICENSE -->
## License
Distributed under the Apache License. See [`LICENSE`](https://github.com/padaiyal/jVaidhiyar/blob/main/LICENSE) for more information.


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/padaiyal/jVaidhiyar.svg?style=for-the-badge
[contributors-url]: https://github.com/padaiyal/jVaidhiyar/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/padaiyal/jVaidhiyar.svg?style=for-the-badge
[forks-url]: https://github.com/padaiyal/jVaidhiyar/network/members
[stars-shield]: https://img.shields.io/github/stars/padaiyal/jVaidhiyar.svg?style=for-the-badge
[stars-url]: https://github.com/padaiyal/jVaidhiyar/stargazers
[issues-shield]: https://img.shields.io/github/issues/padaiyal/jVaidhiyar.svg?style=for-the-badge
[issues-url]: https://github.com/padaiyal/jVaidhiyar/issues
[license-shield]: https://img.shields.io/github/license/padaiyal/jVaidhiyar.svg?style=for-the-badge
[license-url]: https://github.com/padaiyal/jVaidhiyar/blob/master/LICENSE
