# olca-validation
An experiment to get a fast database validation backend into openLCA. The
validation should give errors and warnings for the following things:

* [ ] errors when there are invalid references that can influence calculation
  results (e.g. missing flow, flow property, or unit references in exchanges or
  characterization factors; allocation factors pointing to a product that is
  not part of the process; etc.)
* [ ] errors when there are formula errors
* [ ] warnings when there are invalid references which cannot influence the
  calculation results (e.g. processes referencing sources that do not exist)
