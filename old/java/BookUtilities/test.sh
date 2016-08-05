#!/bin/sh

# Run the different utility services
#curl http://localhost:6464/book/utility/load_catalog
curl http://localhost:6464/book/utility/generate_ratings
#curl http://localhost:6464/book/utility/generate_item_attributes
#curl http://localhost:6464/book/utility/load_predictions

