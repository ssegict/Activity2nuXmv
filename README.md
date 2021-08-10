# Activity2nuXmv Repository

This repository maintains an automatic transformation of Activity diagrams and (extended) Object Life cycles given in UML to nuXmv source code for model checking. Annotated Activity diagrams, using a custom Stereotype, are transformed using a model-to-text transformation (QVTo and Xtend) to nuXmv.

## Repository Structure:

The project contains the following folders:
* __ActivityDiagram2nuXmvModules__ - contains the model-to-code transformation project
* __at.ac.tuwien.ac.at.umlprofile__ - contains the used UML Stereotype
* __nuXmv__ - contains utility files for running the nuXmv model checker

# Project ActivityDiagram2nuXmvModules
Contains the  transformation algorithm from UML to nuXmv. The transformation actually contains two parts: model-to-model (QVTo) transformation for aggregating Activity and object life cycle models (folder __transforms__) and model-to-text (Xtend) for generating nuXmv code (folder __src__). The source-code of the transformation is provided as an Eclipse project. This project requires an initial setup (see Section: Setup Transformation Toolchain) to function properly. 

The project also contains two example models for illustrating the transformation approach: Invoice and EVehicle. These examples are placed in the Folder Model_EVehicle and Model_Invoice respectively. The output of the first model-to-model transformation is located in Model_EVehicle_out and Model_Invoice_out. The final, generated nuXmv code is available in code-generation-evehicle and code-generation-invoice.

For running the transformation we use MWE2 build-scripts. We provide two examples of theses scripts that can be called from eclipse - one for processing the invoice example and one for the E-Vehicle example. In both cases the transformation generates the source-code used with the nuXmv model checker. The output should be (almost) identical to the code-generation-* folders as described above. Almost because the order in which certain commands for nuXmv are constructed is not fixed. However, functionality wise they behave identical.

## Setup Transformation Toolchain

The transformation requires an Eclipse Modeling Installation (version 2021-03) with the following additional packages:
	- Papyrus (version 5.1)
	- Xtend IDE (version 2.25)
	- MWE2 (version 2.12)
	
Newer versions should be working as well.

The Eclipse Modeling package can be downloaded here: https://www.eclipse.org/downloads/packages/

Papyrus: 
-------
Install using the Eclipse Software Repositories.
In Eclipse select:
	"Help" > "Install New Software"
This opens a new dialog for installing software. Here:
	Select  --All Available Sites-- in "Work with" dropdown menu
	Navigate to "Modeling" and select "Papyrus for UML"

Xtend IDE:
---------
Install using the Eclipse Software Repositories.
In Eclipse select:
	"Help" > "Install New Software"
This opens a new dialog for installing software. Here:
	Select  "--All Available Sites-- in "Work with" dropdown menu 
	Navigate to "Programming Languages" and select "Xtend IDE"

MWE2:
----
Install using the Eclipse Software Repositories.
In Eclipse select:
	"Help" > "Install New Software"
This opens a new dialog for installing software. Here:
	Select  "--All Available Sites-- in "Work with" dropdown menu 
	Navigate to "Modeling" and select "MWE2 Language SDK" and "MWE2 Runtime SDK"


## Importing the Projects in Eclipse
Start Eclipse and create a new workspace. 
Import the Eclipse projects contained in the ActivityDiagram2nuXmvModules folder via:
	"File" > "Import"
This opens a new dialog "Import". Here select:
	"General" > "Existing Projects into Workspace"
Choose "Select root directory" and navigate to the ActivityDiagram2nuXmvModules folder.
Select both projects "ActivityDiagram2nuXmvModules" and "at.ac.tuwien.ac.at.umlprofile" and click "Finish".

Now both projects should be available in Eclipse without errors. The project "ActivityDiagram2nuXmvModules" contains example models and the source-code for the transformation. 

## Running the Transformation with the provided Examples

The models for the running example and the E-Vehicle scenario can be reviewed in the "Models_Invoice" and "Models_EVehicle" folders of the "ActivityDiagram2nuXmvModules" project.

The transformation can be launched via two *.mwe2 files located in the src-folder of the "UML2nuXmvTransformation" project. Select one of run_Invoice.mwe2 or run_EVehicle.mwe2, right-click and select "Run As" > "MWE2 Workflow". Check the output window for errors (there shouldn't be any). If the transformation succeeds the resulting files are available in code-generation-evehicle or code-generation-invoice (depending on the selected mwe2 file). These files can then be used with nuXmv.

# Running the Model Checker

The model checker is called via command line, although also an automatic launching via MWE2 files is supported.

## Setup:
For model checking the generated source-code the nuXmv model checker needs to be installed. 

nuXmv can be downloaded here: https://nuxmv.fbk.eu/
For our setup we used the 2.0.0 version in 32- or 64-bit.

Addtionally a setup for a cpp-compiler is necessary. If not present on the system, you can install it via package manager (linux) or mingw on windows (https://osdn.net/projects/mingw/). You can directly download and install the "MinGW.org Compiler Collection". Alternatively you can follow the instructions here: http://win-builds.org/doku.php/download_and_installation_from_windows


## Setting up the generated source-code for model checking
For running the model checker execute the following steps:
1. copy the *modules.h* file from folder *Modules* in project *ActivityDiagram2nuXmvModules* to the *bin* folder of nuXmv. Essentially, place it in the folder of the nuXmv executable.
2. copy the generated nuXmv source-code (by running the transformation the resulting files are placed in a code-generation-* folder within the Eclipse project) to the *"*bin* folder of nuXmv.
	
## Running nuXmv
We included script files for running the model checking tasks of the examples. Call the model checker by executing in a command line:

* nuXmv.exe -source nuXmv_script.scr code-generation-[EXAMPLE]\main.smv
where EXAMPLE is to be replaced with "invoice" for the invoice example *nuXmv.exe -source nuXmv_script.scr code-generation-invoice\main.smv*
* with "evehicle" for the E-Vehicle scenario: *nuXmv.exe -source nuXmv_script.scr code-generation-evehicle\main.smv*
	
The model checker should terminate with something similar like the message below:
	-- terminating with bound 32.
	-- specification ( G !olc.ERROR &  F ( G terminator.state = terminated))    is true
	elapse: 14.32 seconds, total: 14.57 seconds
	
There is a separate script for checking an additional property (a business rule) with the invoice example: 
	nuXmv.exe -source nuXmv_Invoice_script.scr code-generation-invoice\main.smv