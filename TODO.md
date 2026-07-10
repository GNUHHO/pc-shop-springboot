\# Next Task



\## Current status



Completed:



\- GET /api/v1/products/{productId}

\- ProductResponse DTO

\- ProductNotFoundException

\- GlobalExceptionHandler

\- Product ID validation with @Positive

\- HTTP 200, 400 and 404 tests

\- Product tests: 6/6 passed

\- Maven compile: successful



\## Next task



Convert:



GET /api/v1/products



From:



List<Product>



To:



List<ProductResponse>



\## Rules



\- Keep the current URL.

\- Return HTTP 200 with an array.

\- Return HTTP 200 with \[] when no products exist.

\- Do not modify the database.

\- Do not modify ProductRepository.

\- Keep all existing Product tests passing.

