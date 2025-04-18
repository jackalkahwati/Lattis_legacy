Skylock Firmware Project Fact Sheet
===================================

This document is a meant as a quick reference and guide to helping someone start to understand the different parts and pieces of the firmware project for the Skylock product.

##### Engineers:

- Jan worked on this from I don't know when until March 6, 2015. He had some basic functionality working of which some was used for the next implementation. His final work can be found in SKYLOCK\_JAN\_030615.ZIP in the "Archive" folder.
- John Bettendorff worked on this project from March 1, 2015 through ?.

##### Nordic files and Documentation:

I've (John) discovered while trying to learn about this product that several files from Nordic seem to have disappeared since when the previous engineer Jan was working on the project. Obviously that is not good for trying to maintain a project and information on that project for the long term. For example, Jan appeared to work with a version 6 of the Nordic SDK, but when I took over it is now version 7.2 and there is no mention of version 6 anywhere on the Nordic web site. Also, Jan mentions a reference to a specific errata on the Nordic device except the current errata sheet from Nordic only mentions that particular one is now fixed with no explanation of the actual problem (of course the current board still has a CPU that is subject to that error).

Needless to say, all of this is information that is best kept with the project. For that reason the following information from Nordic is stored with the firmware project.

- Each copy of the Software Development Kit that has been used with the project is stored with the project. If a new Software Development Kit is downloaded, the old copy should not be deleted. Currently we are only using include files from Nordic so there is a Nordic include directory that is labeled with the specific version of the Software Development Kit and the entire development kit ZIP file is available in the "Archive" folder.
- The Soft Device from Nordic will obviously continue to change over time. There is a specific folder for the Soft device where the entire contents of the Soft Device Zip file have been expanded. If a new Soft Device is used, just create a new folder labeled with that version and switch the build process over to start using the new Soft Device.
- Errata documents from Nordic or any other chip vendor should be downloaded and stored in the project documentation folder.
- Documentation for all chips used in the project should be downloaded to the documentation folder. If the product has been using a part for some time and the documentation for that part changes it is wise to keep the older version because it may better explain an older parts behavior than newer documentation.

##### Tools:

- Jan was using GCC, not sure what version
- Current development is with IAR Embedded Workbench for ARM 7.30.4

##### Builds:

Once we start creating official builds, list the release build along with the version of Soft Device and development kit that was used to produce the release. Note that this information should be redundant because all of that will be archived with the build, but it is frequently useful to have a summary that can be referenced much quicker than going back through source control.

##### Directory Structure:

The following is the directory structure of the project with a brief description of the contents.

1. Archive: Location of Jan's work and Nordic development kits (ZIP files only)
2. Documentation:
  a. Nordic CPU: Any Nordic Documentation
  b. Support Hardware: Datasheets for external devices

3. IAR\_7\_30\_4: Location of the IAR workbench project files (use to build project)
4. Include: Skylock specific include files
  a. ARMFiles: CMSIS standard include files
  b. NordicFiles\_SDK\_7\_2\_0: Required Nordic include files from development kit

5. SoftDevice\s110\_nrf51822\_7.1.0: Current Soft Device being used
6. Source: Location of Skylock specific source code
