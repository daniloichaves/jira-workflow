# JIRA workflowdata plugin

This plugin adds REST service to JIRA that shows data on currently configured
workflow schemes and workflows.

## Location

$JIRA_BASE/rest/workflowdata/1.0/workflowscheme

## Running in the development environment

Assuming you have the Atlassian SDK installed, run the following in the
root of a clone:

    $ atlas-run

## Packaging
Assuming you have the Atlassian SDK installed, run the following in the
root of a clone:

    $ atlas-package

This creates the plugin artefact in the `target/` directory.

## Licensing

This plugin is made available under the Apache 2.0 licence.

## Acknowledgements

The code in this plugin is based on [testkit] which is available under the
Apache 2.0 licence.

[testkit]: https://bitbucket.org/atlassian/jira-testkit
