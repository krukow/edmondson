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
[psych_safety_generative_culture.ipynb](../examples/google_sheets/psych_safety_generative_culture.ipynb).

### Authenticate
To make sure the example works, first generate an OAUTH token in the `tokens`
directory of this project's root folder:

    GOOGLE_CREDENTIALS_JSON=./credentials.json clj -X:google-oauth2 :token-directory tokens

You may be asked to authenticate with Google. When finished you should see:

    Token stored in:  tokens

### Optional: Customize survey
If you want to use your own survey rather than the example survey with fake data, set the following
environment variables

* `RESULTS_URL="link"`: replace "link" with the link to the Google Sheet containing survey results (default value: https://docs.google.com/spreadsheets/d/1S_p5d9YrPg1_sawbhhTRNPJPfvnRweFmC4-OxvYhzao/edit?usp=sharing)
* `TEAM_NAME=Spidercats`: replace "Spidercats" with a team name to customize the report
* `NUM_PARTICIPANTS=24`: replace "24" with the total team size (number of people who have received the survey link)

### Explore with Jypyter lab
Set these environment variables and run `jupyter lab` e.g.

```bash
    RESULTS_URL="https://docs.google.com/spreadsheets/d/1S_p5d9YrPg1_sawbhhTRNPJPfvnRweFmC4-OxvYhzao/edit\?usp\=sharing" NUM_PARTICIPANTS="66"  TEAM_NAME="Example" jupyter lab
```
This should open your browser with URL: http://localhost:8888/lab.

In the browser, open the file: `examples` / `google_sheets` /
`psych_safety_generative_culture.ipynb` using the kernel `Clojure (edmondson)`.

Run the example notebook! If you need help navigating the Jupyter notebook,
check out [this overview documentation
page](https://jupyterlab.readthedocs.io/en/stable/getting_started/overview.html).

We encourage exploration by modifying each sections contents and re-evaluating.

## Generating a report
Run the following (customize as needed):

```bash
export NUM_PARTICIPANTS="39"
export TEAM_NAME="My team"
./script/analyze.sh examples/google_sheets/psych_safety_generative_culture.ipynb
```

You should see something like
```
Building
Building uber jar: target/Edmondson-standalone.jar
Installing
WARNING: When invoking clojure.main, use -M
Clojupyter v0.3.2 - Remove kernel 'edmondson'

    Step: Delete /Users/krukow/Library/Jupyter/kernels/edmondson

    Status: Removals successfully completed.

exit(0)
WARNING: When invoking clojure.main, use -M
Clojupyter v0.3.2 - Install local

    Installed jar:	target/Edmondson-standalone.jar
    Install directory:	~/Library/Jupyter/kernels/edmondson
    Kernel identifier:	edmondson

    Installation successful.

exit(0)
Generating html report from: examples/google_sheets/psych_safety_generative_culture.ipynb
[NbConvertApp] Converting notebook examples/google_sheets/psych_safety_generative_culture.ipynb to html
[NbConvertApp] Executing notebook with kernel: edmondson
[E 08:21:28.668 Clojupyter] c8r.zmq.heartbeat-process -- heartbeat: Polling ZeroMQ socket returned negative value - terminating.
/Users/krukow/opt/anaconda3/lib/python3.8/site-packages/nbconvert/filters/datatypefilter.py:39: UserWarning: Your element with mimetype(s) dict_keys(['application/vnd.vegalite.v3+json']) is not able to be represented.
  warn("Your element with mimetype(s) {mimetypes}"
[NbConvertApp] Writing 597812 bytes to results/html/report.html
report.html
```

From the browser open the file: `results/html/report.html`