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

## Three modes: GitHub Codespaces, Docker-local, Local

You can run Jupyter either through local-machine development, using Docker
locally on your machine, or using Docker and [GitHub Codespaces](https://github.com/features/codespaces).

All three modes require you to first follow the instructions in the README
on the frontpage: https://github.com/krukow/edmondson#4-api-access to generate a
credentials JSON file and run `clj -X:google-oauth2` to generate `tokens/StoredCredential`.

## GitHub Codespaces

****WARNING****: Running in Codespaces requires you to temporarily upload your
credentials.json to the Azure VM running the Codespace as well as running the
Jupyter notebook on a public IP (with a password though). If you're not
comfortable with this, we recommend one of the other options.

This is the easiest mode since it requires no local installation of dependencies.

On GitHub fork the repo and click Code > Codespaces > New code space.

You should see ![Codespaces loading screenshot](https://user-images.githubusercontent.com/3635/137152796-07639ba9-d6bb-4ba4-8555-46e966317efc.png).

Once Codespaces is open run the following in a terminal

``` bash
$ jovyan@codespaces_82e6c5:/workspaces/edmondson$ ./script/go.sh docker
Building
Building uber jar: target/Edmondson-standalone.jar
Installing
WARNING: Implicit use of clojure.main with options is deprecated, use -M
Clojupyter v0.3.2 - Remove kernel 'edmondson'

    Step: Delete /home/jovyan/.local/share/jupyter/kernels/edmondson

    Status: Removals successfully completed.

exit(0)
WARNING: Implicit use of clojure.main with options is deprecated, use -M
Clojupyter v0.3.2 - Install local

    Installed jar:      target/Edmondson-standalone.jar
    Install directory:  ~/.local/share/jupyter/kernels/edmondson
    Kernel identifier:  edmondson

    Installation successful.

exit(0)
```

For authentication/authorization you must upload the credentials.json file
that you generated in the README on the frontpage: https://github.com/krukow/edmondson#4-api-access

- In the Codespaces browser tab, upload `client_secret...` to the top-level directory, and `tokens/StoredCredential` into the `tokens` directory.
- In a terminal run `mv client_secret_....json ./credentials.json`

Now in a terminal in Codespaces, run

``` bash
 $ ./lab
...
    http://127.0.0.1:8889/lab?token=a2eae6494223edc81e6cf797735b10468148912590dcb8c0
```

Codespaces should forward port 8889. Click "Open in browser" button in codespaces.

You may see "Token authentication is enabled". In this case enter the token that
is output (in the above example: a2eae6494223edc81e6cf797735b10468148912590dcb8c0).

You should now see the Jupyter lab web-app.

In the Jupyter lab web app file browser, navigate to `examples/google_sheets/psych_safety_generative_culture.ipynb`.

Click the Run menu and select "Run All Cells". You should see the notebook being
populated (This can take a while the first time you run).

Things are now working!

It's beyond the scope of these instructions to dig into how to further use
Jupyter and Codespaces.


## Docker-local

This requires Docker installed locally on your machine.

Run

``` bash
docker run -it -u jovyan -p 8888:8888 -p 8889:8889 \
            -v ${PWD}:/home/jovyan/work/ \
            -w /home/jovyan/ \
            krukow/edmondson:v1.3.3-dev6 \
            /bin/bash
```

Then in the container, run

``` bash
jovyan@810490d944d0:~$ cd work/
jovyan@810490d944d0:~/work$ ./script/go.sh docker
...
```

Similarly to the Codespaces example above, run

``` bash
jovyan@810490d944d0:~/work$ mv client_secret_....json ./credentials.json
jovyan@810490d944d0:~/work$ ./lab
...
   http://127.0.0.1:8889/lab?token=ca2da8c803a6336e2ddde6caef55f595fde94d7e4cb9d6eb
```

Opening the URL that was printed (http://127.0.0.1:8889/lab?token=ca2da8c803a6336e2ddde6caef55f595fde94d7e4cb9d6eb)
should give you the Jupyter lab web app UI.

In the Jupyter lab web app file browser, navigate to `examples/google_sheets/psych_safety_generative_culture.ipynb`.

Click the Run menu and select "Run All Cells". You should see the notebook being
populated (This can take a while the first time you run).

Things are now working!

It's beyond the scope of these instructions to dig into how to further use
Jupyter.

## Local: Prerequisites

[Install Jupyter](https://jupyter.org/install). This guide uses the Anaconda
distribution (see: https://www.anaconda.com/products/individual).

Check that you can start Jupyter lab by running `jupyter lab`.

### Build kernel

To build an Jupiter Clojure kernel with Edmondson support, run
`./script/go.sh`.

## Running the example
In this section we run the example psychological safety report notebook:
[psych_safety_generative_culture.ipynb](../examples/google_sheets/psych_safety_generative_culture.ipynb).


Copy or move your to `credentials.json`:

``` bash
$ mv client_secret_....json ./credentials.json
```

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

From the browser open the file: `results/html/report.html`.

To customize the report look at the script `./script/analyze-actions-fast.sh`.
