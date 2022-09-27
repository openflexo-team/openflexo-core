/images/components/fml-core/FMLCore] Openflexo-core

# Openflexo infrastructure to support federation

Each business manages its own set of information, domain specific languages, tools, models...
  
Extending, combining, synchronizing, representing them in a suited environment without duplicating this set of data is therefore key.
  
Openflexo offers a dedicated language (Flexo Modeling Language or FML) for defining domain-specific perspectives in relation to heterogeneous and separated technologies. 
  
Besides it provides a set of mecanisms for manipulating and representing federated informations.
  
## Virtual Models, Flexo Concepts, Flexo Roles and Flexo Behaviours

In order to manipulate informations Openflexo aggregates them in an atomic notion called Flexo Concept.
  
A Flexo Concept associates various informations from different information sources(models, metamodels, graphical representation, GUI, spreesheets etc…). 
  
These associations are called a Flexo Roles. A Model Slot is a reference pointing a kind of information resource(Excel sheet, EMF metamodel...), or even a Virtual model.

Flexo Concepts can be grouped by concerns within a modeling space named Virtual Model.

Openflexo provides a support to manipulate Flexo Concepts. Thereby Flexo Behaviours represent manipulation primitives, such as creation, deletion, access (navigation), etc… (see Working with Flexo Behaviours)

In Openflexo a Viewpoint provides the convention, rules and technologies for constructing, presenting and analysing Views. A Viewpoint aggregated a set of Virtual models.

An Openflexo View is the instantiation of a particular Viewpoint with its own Objective relevant to some of the preoccupations of the Viewpoint.

Openflexo Core depends on [Connie](https://openflexo.org/connie/), [Pamela](https://openflexo.org/pamela/), [Diana](https://openflexo.org/diana/) and [Gina](https://openflexo.org/gina/) projects.
  
## Contents of this package

Openflexo Core Library mainly contains two parts:

The "model-layer":

* [FlexoFoundation](/flexo-foundation/index.md), which contains the core code for FML

* [FML parser](/fml-parser/index.md), which is an implementation for FML parser

* [FlexoOntology](/flexo-ontology/index.md), which is an extension of fml-core dedicated to "strict-modelling" tools

* [FlexoDocumentation](/flexo-documentation/index.md), which is an extension of fml-core dedicated to formatted text tools

The "UI-layer" (Swing):

* [Flexo-UI](/flexo-ui/index.md), which contains the core code for FML-core Swing tooling

* [FML-UI](/fml-technology-adapter-ui/index.md), which provides Swing tooling for FML

* [FML@runtime-UI](/fml-rt-technology-adapter-ui/index.md), which provides Swing tooling for FML@runtime

* [FlexoOntology-UI](/flexo-ontology-ui/index.md), which provides Swing tooling for FlexoOntology extension

* [FlexoDocumentation-UI](/flexo-documentation-ui/index.md), which provides Swing tooling for FlexoDocumentation extension
  
   
  
