[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=bugs)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=coverage)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)

# API extension for Polarion ALM

This Polarion extension provides additional functionality which is not implemented in standard Polarion API for some reason.

> [!IMPORTANT]
> Starting from version 2.0.0 only latest version of Polarion is supported.
> Right now it is Polarion 2506.

## Custom field for project

Polarion project does not support setting custom fields out of the box.
This API extension can be used to solve this problem.

This API can be called using REST API and in Velocity Context.

## Quick start

The latest version of the extension can be downloaded from the [releases page](../../releases/latest) and installed to Polarion instance without necessity to be compiled from the sources.
The extension should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.api-extender/eclipse/plugins` and changes will take effect after Polarion restart.
> [!IMPORTANT]
> Don't forget to clear `<polarion_home>/data/workspace/.config` folder after extension installation/update to make it work properly.

## Documentation

- [Development Guide](./DEVELOPMENT.md) - Comprehensive guide for setting up development environment and contributing to this project
- [Contributing Guidelines](./CONTRIBUTING.md) - Guidelines for contributing to this project
- [Coding Standards](./CODING_STANDARDS.md) - Coding standards and best practices
- [Release Process](./RELEASE.md) - Information about the release process

## Polarion configuration

### REST API

This extension provides REST API. OpenAPI Specification can be obtained [here](docs/openapi.json).

### Live Report Page

Get version:

```velocity
#set ($version = $customFieldsProject.getVersion())
#if ($version)
    API Extender version = $version
#end
```

or

```velocity
#set ($version = $globalRecords.getVersion())
#if ($version)
    API Extender version = $version
#end
```

Get custom field value:

```velocity
#set ($field = $customFieldsProject.getCustomField('elibrary', 'custom_field'))
#if ($field)
    $field.getValue()
    <br>
#end
```

Get global record value:

```velocity
#set ($field = $globalRecords.getRecord('record_name'))
#if ($field)
    $field.getValue()
    <br>
#end
```

Due to Polarion limitations we are not able to save custom fields in Live Report Page using Velocity, but we can use JavaScript for this.

Set custom field value:

```html
<input id='cfp_project' type='text' value='elibrary' name='project'/>
<input id='cfp_key' type='text' value='custom_field' name='key'/>
<input id='cfp_value' type='text' value='custom_value' name='value'/>
<script>
    function save_project_custom_field() {
        const project = document.getElementById('cfp_project').value;
        const key = document.getElementById('cfp_key').value;
        const value = document.getElementById('cfp_value').value;
        const requestBody = (value === null || value === "") ? "" : JSON.stringify({'value': value});

        fetch('/polarion/api-extender/rest/internal/projects/' + project + '/keys/' + key, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: requestBody
        })
                .then(response => {
                    if (response.ok) {
                        return "Saved!"
                    } else {
                        return response.text()
                    }
                })
                .then(text => {
                    alert(text)
                });
    }
</script>
<button onclick='save_project_custom_field()'>Save</button>
```

Set global record value:

```html
<input id='record_key' type='text' value='record_name' name='record_name'/>
<input id='record_value' type='text' value='record_value' name='record_value'/>
<script>
    function save_record() {
        const key = document.getElementById('record_key').value;
        const value = document.getElementById('record_value').value;
        const requestBody = (value === null || value === "") ? "" : JSON.stringify({'value': value});

        fetch('/polarion/api-extender/rest/internal/records/' + key, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: requestBody
        }).then(response => {
            if (response.ok) {
                return "Saved!"
            } else {
                return response.text()
            }
        }).then(text => {
            alert(text)
        });
    }
</script>
<button onclick='save_record()'>Save</button>
```

Note that internal API in URL should be used.

### Classic Wiki Page

Get custom field value:

```velocity
#set($projects = $projectService.searchProjects("","id"))

#foreach($project in $projects)
    #set($projectId = $project.id)
    #set($field = $customFieldsProject.getCustomField($projectId, 'custom_field'))
    #if ($field)
        $projectId custom_field = $field.getValue()
        <br>
        #set($field = false)
    #end
#end
```

Get global record value:

```velocity
$globalRecords.getRecord('record_name')
```

Set custom field value:

```velocity
$customFieldsProject.setCustomField('elibrary', 'custom_field', 'new_value')
```

Set global record value:

```velocity
$globalRecords.setRecord('record_name', 'record_value')
```

## Tools
This extension includes several velocity tools to facilitate development and maintenance.

### Collections Tool

Sort a map by its values in ascending or descending order:
```velocity
#set($sortedMap = $collectionsTool.sortMap($unsortedMap, true))
```

Wrap given list of objects into a IPObjectList:
```velocity
#set(list = $collectionsTool.convertArrayListToWeakPObjectList($workItems))
```

### Regex Tool

Extract regex matches in the given text, returning the matches and their respective groups:
```velocity
#set($matches = $regexTool.findMatches('(\d+)', "test123and456"))
```
