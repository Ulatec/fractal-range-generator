<h1>Fractal Range Generator</h1>

<p>An application that attempts to use fractal geometry and calculus to create a range of probabilistic outcomes for the price of a publicly traded security on a daily basis. As well the application generates multifactor and multi-duration trends.</p>

<p>The purpose of this application is to find the extremities of risk in a given security.</p>

<p>Original idea inspired by <a href="https://www.hedgeye.com/">Hedgeye Risk Management</a>.</p>

# Notes

<li>Establishes multi-duration trends based upon the rate of change of realized volatility, implied volatility, implied volatility discount and volume</li>
<li>Calculates the implied volatility of the security based upon 40 days to expiration at-the-money put contracts</li>
<li>Adjusts historical option contracts and closing prices for stock splits.</li>
<li>Stores treasury rates locally. Need a new datasource for this.</li>
<li>Incoming historical data is provided by <a href="polygon.io">Polygon.io</a></li>
<li>Multi-threaded for individual requests/Calculating Implied Volatility/Requesting Data from Polygon</li>
<li>Configured for Eureka Naming Server and Spring Config Server</li>


# Frontend
<p>Front end is part of a multi-project front end at the link below.</p>

https://github.com/Ulatec/MultiProjectFrontend
