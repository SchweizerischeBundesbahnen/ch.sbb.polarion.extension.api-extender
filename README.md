# API extension for Polarion ALM

This Polarion extension provides additional functionality which is not implemented in standard Polarion API for some reason.

## Custom field for project

Polarion project does not support setting custom fields out of the box.
This API extension can be used to solve this problem.

This API can be called using REST API and in Velocity Context.

## Build

This extension can be produced using maven:
```bash
mvn clean package
```

## Installation to Polarion

To install JSON editor extension to Polarion `ch.sbb.polarion.extension.json.editor-<version>.jar`
should be copied to `<polarion_home>/polarion/extensions/json-editor/eclipse/plugins`
It can be done manually or automated using maven build:
```bash
mvn clean install -P install-to-local-polarion
```
For automated installation with maven env variable `POLARION_HOME` should be defined and point to folder where Polarion is installed.

Changes only take effect after restart of Polarion.

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
