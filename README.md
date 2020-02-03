# JavaMMonitTrayIcon
When I started this project, my idea was to watch my servers, which use mmonit as monitoring tool, from a single flag in the tray bar. This evolved to a much more generic tool, that uses JQ language to parse JSON from any API and is capable of generic authentication. I still use it to monitor my MMonit servers, but it is now much easier to configure and much more useful for many other people.

## Building

It's a standard maven project. Just clone the repo and "package" it.

```
$ git clone https://github.com/girino/JavaMMonitTrayIcon.git
$ cd JavaMMonitTrayIcon
$ mvn package
```

This should do the trick. Binary will be on your "target" folder

## Running

On Operating systems that support java, just click on the generated jar file. If you prefer using the command line:

```
$ java -jar mmonit-tray-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

## Configuring

When run for the first time, the settings window will be automatically shown. You need to fill in at least the server url and the fields necessary for authentication. If using "Basic" authentication, use the URL syntax for user and password:

``https://user:password@www.example.com``

In case of form authentication, you need to open the form source and check what are the fields for login, password, and any other hidden fields. You can add as many fields as you need. The default values for an mmonit server are preconfigured. 

### Pages
There is a fixed set of pages you need to configured, if you are using FORM authentication:

- Init: The login page, where the login form is available.
- Login: The page to where the login form is submitted. (see "action" property from the "form" tag in teh login page).
- Logout: The page used for login out users.
- API: the url where you are getting the data to be displayed.

Again, the default values are all set for an mmonit server.

### Rules

The most important part of the settings, Here you will attribute a color to a JQ language string. You can see how JQ language works here: https://stedolan.github.io/jq/tutorial/

The rules are executed in order, and are expected to return a numeric (integer) value. The first non-zero value to be evaluated will be the color of the display flag. If your API does not return numbers, make sure to convert it to numbers. Examples:

input file:
```
{ 
	"red": "enabled",
	"green": "enabled"
}
```

Rules:

| Color | Rule |
| ----- | ---- |
| RED   | `.red == "enabled" \| if . then 1 else 0 end` |
| GREEN | `.green == "enabled" \| if . then 1 else 0 end` |

This will display a RED flag, because RED rule is above the GREEN rule, and thus, is executed first. Note that we need an "if" conditional to convert the boolean to an integer.

## The config file.

You can find the config file in the default user folders (%LocalAppData%/JavaMMonitTrayIcon on windows, \~/Library/Application Support/JavaMMonitTrayIcon on OSX, and \~/.config/JavaMMonitTrayIcon on linux). Its a regular Properties file, you can edit it directly if you want, but it gets messy because the java "Properties" class doesn't care about the original order of fields.

# Licensing

This software is licensed under my own "Girino's Anarchist License", available in full at https://girino.org/license/. You Just need to keep the copyright notices and can do whatever you please with it. I am not liable for any damages caused by you or any person using this code.

# Disclaimer

I wrote this software for my own use and am sharing it "as is", so please be patient if there are any bugs. Or better yet, fix them for me. I'd be happy to incorporate any code you submit, as long as it works and is well written.

