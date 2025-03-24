# Silassaq - Greenland Weather App

WIP - Not Functiona ATM

Silassaq is a weather application specifically designed for Greenland, providing accurate weather forecasts for various locations across the country. The app integrates with the MET Norway Weather API to deliver reliable weather data.

## Features

- Weather forecasts for multiple Greenland locations
- Detailed current weather conditions
- Multi-day forecasts
- Support for multiple languages (English, Danish, Kalaallisut)
- Offline mode with cached weather data
- Aurora forecast
- Sea ice conditions
- Daylight visualization

## API Integration

This application uses the MET Norway Locationforecast/2.0 API to retrieve weather data. The implementation follows MET Norway's best practices:

- Proper User-Agent identification
- Caching with If-Modified-Since headers
- Coordinate precision limited to 4 decimal places
- Retry mechanism with exponential backoff
- Comprehensive error handling

## Technical Details

- Built with Kotlin for Android
- Uses Retrofit for API communication
- Implements MVVM architecture
- Jetpack Compose for the UI

## Setup

To run this project, you'll need:

1. Android Studio
2. Kotlin development environment
3. Internet connection for API access

## Contributing

Contributions to improve Silassaq are welcome. Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- MET Norway for providing the weather data API
- GeoNames for location data
