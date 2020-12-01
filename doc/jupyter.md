# Jupyter support

Edmondson can run within a Jupyter lab environment through Clojure kernel
support (using
[clojupyter/clojupyter](https://github.com/clojupyter/clojupyter)). 

Jupyter notebooks are documents that combine live runnable code with narrative
text (Markdown), equations (LaTeX), images, interactive visualizations and other
rich output. You can run notebooks like [this
example](../examples/google_sheets/psych_safety.ipynb) in Jupyter lab.

This allows you to build interactive reusable reports for survey read-out
presentations (e.g. sharing the results of a psychological safety survey with a
team).

## Prerequisites

[Install Jupyter](https://jupyter.org/install). This guide uses the Anaconda
distribution (see: https://www.anaconda.com/products/individual).

Check that you can start Jupyter lab by running `jupyter lab`.


### Build kernel
To build an Jupiter Clojure kernel with Edmondson support, run
`./script/build.sh` to produce an "uber jar": 

    ./script/build.sh
    Cleaning build
    Building uber jar: target/Edmondson-standalone.jar

### Install kernel
From the root folder of this project, run:

    clj -A:jupyter -m clojupyter.cmdline install --ident edmondson --jarfile target/Edmondson-standalone.jar

If you get an error "Error: A Clojupyter kernel named 'edmondson' is already
installed.", then you can remove this previous installation with: 

    clj -A:jupyter -m clojupyter.cmdline remove-install edmondson

See also [clojupyter/clojupyter](https://github.com/clojupyter/clojupyter) for
more commands for managing the kernels.

When successfully installed, you should see something like:

    Clojupyter v0.3.2 - Install local

    Installed jar:  target/Edmondson-standalone.jar
    Install directory:  ~/Library/Jupyter/kernels/edmondson
    Kernel identifier:  edmondson

    Installation successful.

## Running the example
In this section we run the example psychological safety report notebook:
[psych_safety.ipynb](../examples/google_sheets/psych_safety.ipynb).

### Authenticate
To make sure the example works, first generate an OAUTH token in the `tokens`
directory of this project's root folder:

    GOOGLE_CREDENTIALS_JSON=./credentials.json clj -X:google-oauth2 :token-directory tokens

You may be asked to authenticate with Google. When finished you should see:

    Token stored in:  tokens

Now start Jupyter lab with the command `jupyter lab` which should open your
browser with URL: http://localhost:8888/lab.

In the browser, open the file: `examples` / `google_sheets` /
`psych_safety.ipynb` using the kernel `Clojure (edmondson)`.

Run the example notebook! If you need help navigating the Jupyter notebook,
check out this documentation page:
https://jupyterlab.readthedocs.io/en/stable/user/notebook.html.

We encourage exploration by modifying each sections contents and re-evaluating.