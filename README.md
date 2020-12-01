# Edmondson
An extensible, easy-to-use toolkit for analyzing and scoring survey constructs
like psychological safety and generative culture. Supports multiple
survey-systems like Google Forms and Qualtrics.

# Quick start
This section serves as a quick guide for getting started. Please refer to the
[doc](./doc) for more documentation.

## Prerequisites


### Java/JDK
You must have Java/JDK installed (e.g.
[adoptopenjdk.net](https://adoptopenjdk.net/)).

On OS X, I have:

```bash
export JAVA_HOME=`/usr/libexec/java_home`
export PATH=$JAVA_HOME/bin:$PATH
```

### Clojure CLI
You must also have [Clojure and Clojure CLI tools
installed](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools).

### Clone this repo
Clone this repo into a local directory on your machine.

### API access
You'll need API access to a survey service provider. In this example, we'll use
Google.

* Find the Google Sheets API Java Quickstart page ([currently
  here](https://developers.google.com/sheets/api/quickstart/java)).
* Complete ["Step
  1"](https://developers.google.com/sheets/api/quickstart/java#step_1_turn_on_the)
  to generate `credentials.json`.
* You don't need to other steps. Just download and save `credentials.json` to
  your working directory.
* Set the environment variable `GOOGLE_CREDENTIALS_JSON` to the path to
  `credentials.json`. For example, in a bash shell session:

      export GOOGLE_CREDENTIALS_JSON=`pwd`/credentials.json

## Run an example
To test that everything works as expected, you can run one of the examples in
the `examples` directory. There are two main ways to do this: using a [Clojure
CLI REPL](https://clojure.org/reference/deps_and_cli), and using
[Jupyter](https://jupyter.org/). In this quick-start guide we'll do the former
because Jupyter requires installing more software (although it's quite cool!).

### The Survey
The example uses a Google Forms survey on team health (with dummy data). You can
see the form here: 

* Survey link: https://forms.gle/ByXsuvB614vopVVF7
* [Survey results as Google
  sheet](https://docs.google.com/spreadsheets/d/1QkBeMNGfsHHga85c-UsLAwnpmz7QyhvFK_n31CzDe7c/edit?usp=sharing).
* If you want to make a copy of this form, [use this
  link](https://docs.google.com/forms/d/1iaECjHrGRd1uZsl7IlPktHnDj9xZLqNoUjcicvwDUY0/edit?usp=sharing).

### Analyzing the data with a Clojure REPL
Start the example with the following command. 

(Note: the first time this runs, it takes a while because it downloads project
dependencies on the fly.)

    clj -A:examples -m google-sheets.psych-safety

Your browser will open, asking you to authenticate with a Google account. This
is required to generate an OAUTH token to access the API.

If this doesn't work, check that you've set up the environment variable
`GOOGLE_CREDENTIALS_JSON`. 

You should see something similar to:

    WARNING: When invoking clojure.main, use -M
    Please open the following address in your browser:
    https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=1060853483984-8cv31681n8ll0onat045mgsrt2hgt9i5.apps.googleusercontent.com&redirect_uri=http://localhost:8888/Callback&response_type=code&scope=https://www.googleapis.com/auth/spreadsheets.readonly
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
Copyright © 2020 Karl Krukow and contributors.

All rights reserved. The use and distribution terms for this software are
covered by the MIT LICENSE which can be found in the file LICENSE at the root of
this distribution. By using this software in any fashion, you are agreeing to be
bound by the terms of this license. You must not remove this notice, or any
other, from this software.