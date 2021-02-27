# olca-validation

This is an experiment for a multi-threaded database validation back-end for openLCA. The idea is that the validation starts `n` worker threads that push validation items (basically `error`, `warning`, and `ok` with a message) to a blocking queue. With the blocking queue the validation synchronizes the output of the workers into a set of collected items. When a worker is finished, it sends a finish marker to the blocking queue. The validation is finished when it received the `n` finish markers from the worker threads. 

![](C:\Users\Win10\Projects\openLCA\repos\olca-validation\images\how_it_works.png)

There is also a synchronized stop flag. Before starting a larger chunk of work a worker should check this flag and if the flag indicates that the validation has stopped the worker should immediately send a finish a marker and stop its work. Also, before adding an item to the queue a worker should check this flag and, if the validation stopped, add the item followed by a finish marker and stop its work. The stop flag is used, for example, when the validation was cancelled or when the number of collected items exceeds a given limit.



An experiment to get a fast database validation backend into openLCA. The
validation should give errors and warnings for the following things:

* [ ] errors when there are invalid references that can influence calculation
  results (e.g. missing flow, flow property, or unit references in exchanges or
  characterization factors; allocation factors pointing to a product that is
  not part of the process; etc.)
* [ ] errors when there are formula errors
* [ ] warnings when there are invalid references which cannot influence the
  calculation results (e.g. processes referencing sources that do not exist)
