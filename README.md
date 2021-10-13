# Edmondson
An extensible, easy-to-use toolkit for analyzing and scoring survey constructs
like psychological safety and generative culture. Extensible to support multiple
survey-systems like Google Forms or Qualtrics (supports only Google Forms
currently).

# Quick start
This section serves as a quick guide for getting started. Please refer to the
[doc](./doc) for more documentation.

## Prerequisites - running locally
You don't need to understand Java or Clojure to use this. These are merely
runtime dependencies that need to be installed to run.

### 1. Java/JDK
You must have Java/JDK installed (e.g.
[adoptopenjdk.net](https://adoptopenjdk.net/)).

**NOTE**: If you are running OS X, you may be able to skip this step.

On OS X, I have:

```bash
export JAVA_HOME=`/usr/libexec/java_home`
export PATH=$JAVA_HOME/bin:$PATH
```

### 2. Clojure CLI
You must also have [Clojure and Clojure CLI tools
installed](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools).

### 3. Clone this repo
Clone this repo into a local directory on your machine.

### 4. API access
You'll need API access to a survey service provider. In this example, we'll use
Google.

* Find the Google Sheets API Java Quickstart page ([currently
  here](https://developers.google.com/sheets/api/quickstart/java)).
* Complete ["Prerequisites (gradle not needed)"](https://developers.google.com/sheets/api/quickstart/java#prerequisites) to generate `credentials.json`.

    * Enable the Google sheets API (and optionally the Google Drive API)
    * Click "Create credentials" in APIs & Services menu (Credentials). Create an Create OAuth client ID.
    * Add Google sheets scope: auth/spreadsheets.readonly (optionally Google Drive API scope ../auth/drive.file).
    * Just download and save JSON file.
    to your working directory.
    * Set the environment variable `GOOGLE_CREDENTIALS_JSON` to the path to
  `credentials.json`. For example, in a bash shell session:

```
      export GOOGLE_CREDENTIALS_JSON=`pwd`/client_secret_....json
```

## Test authorization

To test that your OAuth app is set up run

    $ clj -X:google-oauth2

You should see something like:

``` bash
Please open the following address in your browser:
  https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=10...
Attempting to open that address in the default browser now...
```

Authenticate using the same account that created the A Google Cloud Platform project.

Accept that app access and you should see "Received verification code. You may now close this window."

The terminal should now show

``` bash
Token stored in:  ./tokens
```

You have succesfully authenticated and can try the example below.

## Run an example
To test that everything works as expected, you can run one of the examples in
the `examples` directory. There are two main ways to do this:

1. using a [Clojure CLI REPL](https://clojure.org/reference/deps_and_cli); and
2. using [Jupyter](https://jupyter.org/).

In this quick-start guide we'll do the former because Jupyter requires
installing more software (although it's quite cool!). For Jupyter instructions
see [./doc/jupyter.md](./doc/jupyter.md).

### The Survey
The example uses a Google Forms survey on team health (with dummy data). You can
see the form here:

* Survey link: https://forms.gle/kVVMKEpbBZKsDwc8A
* [Survey results as Google
  sheet](https://docs.google.com/spreadsheets/d/1S_p5d9YrPg1_sawbhhTRNPJPfvnRweFmC4-OxvYhzao/edit?usp=sharing).
* If you want to make a copy of this form, [use this
  link](https://docs.google.com/forms/d/1ypwK5o1R1isZbXmeC5NCkz2X3VSLoEY-whXvqAMFGv8/edit?usp=sharing).

### Analyzing the data with a Clojure REPL

Here is an example using Google Sheets:

1. Set the environment variable `RESULTS_URL` to the survey results Google Sheet
   (or leave blank to use the default example link).

2. Execute the script

(Note: the first time this runs, it takes a while because it downloads project
dependencies on the fly.)

    clj -A:examples -m google-sheets.psych-safety

3. Run a report

You should see something similar to:

    WARNING: When invoking clojure.main, use -M

    Attempting to open that address in the default browser now...

    Try (report X) where X is one of the following:
    ("Psychological safety"
    "Generative culture"
    "Psychological safety domains"
    "Open-ended feedback")
    google-sheets.psych-safety=>

This is good - it means that things are working.

You now have a Clojure REPL with some pre-loaded data and functions. Even if
you're not familiar with Clojure, you can use this to explore the data and
functionality. Try typing `(report "Psychological safety")` and press enter. You
may need to scroll up, but you should see something like:

    google-sheets.psych-safety=> (report "Psychological safety")
    Psychological safety

    Overall scores

    Total score:  34.7
    Mean score:  5.0
    Score stddev:  0.3

    Worst responses scored
    (3.0 3.0 3.0 3.0 3.0)

    Best responses scored
    (5.6 5.6 5.6 5.6 5.6)

    Response score stddev
    1.1
    ...

While you may not understand the scores yet, what you are seeing is an analysis
of psychological safety in the example survey data.

That's it - things are working now! Now check out "Next steps" below.

## Next steps
One of the following might be good next steps:

* Read [the documentation](./doc) to better understand features and scoring.
* Explore [the psychological safety report](./doc/jupyter.md) in the example
  Jupyter notebook.
* Set up [a development environment and contribute](./doc/contributing.md)!


# Copyright and License
Copyright © 2021 Karl Krukow and contributors.

All rights reserved. The use and distribution terms for this software are
covered by the MIT LICENSE which can be found in the file LICENSE at the root of
this distribution. By using this software in any fashion, you are agreeing to be
bound by the terms of this license. You must not remove this notice, or any
other, from this software.
