# Silassaq - Greenland Weather App

Silassaq is a weather application specifically designed for Greenland, providing accurate weather forecasts for various locations across the country. The app supports dual API integration with intelligent fallback mechanisms.

## Features

- **21 Greenland Locations**: From Nuuk to Qaanaaq, covering all major cities
- **Dual API Support**: Met Norway (primary) and WeatherAPI.com (fallback)
- **Offline Mode**: Smart caching with age tracking
- **Multi-language Support**: English, Danish, and Kalaallisut
- **Greenland-Specific Features**:
  - Aurora forecast visualization
  - Sea ice conditions monitoring
  - Daylight/twilight tracking (including polar day/night)
- **Detailed Weather Data**: Current conditions, hourly forecasts, multi-day forecasts

## API Integration

The app integrates with two weather APIs for redundancy:

### Met Norway Locationforecast/2.0 (Primary)
- No API key required
- Follows MET Norway's best practices:
  - Proper User-Agent identification
  - Caching with If-Modified-Since headers
  - Coordinate precision limited to 4 decimal places
  - Exponential backoff retry mechanism

### WeatherAPI.com (Fallback)
- Requires free API key
- Automatic fallback when Met Norway is unavailable
- Comprehensive error handling

## Technical Details

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose
- **Network**: Retrofit with OkHttp
- **Async**: Kotlin Coroutines
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34

## Setup

### Prerequisites

1. Android Studio (Arctic Fox or later)
2. Kotlin 2.0+
3. Internet connection for API access

### API Key Configuration

1. Get a free API key from [WeatherAPI.com](https://www.weatherapi.com/signup.aspx)
2. Copy the template file:
   ```bash
   cp app/src/main/java/com/example/silassaq/data/ApiKeys.kt.template \
      app/src/main/java/com/example/silassaq/data/ApiKeys.kt
   ```
3. Open `ApiKeys.kt` and replace `your_weatherapi_key_here` with your actual API key
4. The app will work with Met Norway by default; WeatherAPI.com is used as fallback

**Note**: `ApiKeys.kt` is gitignored to keep your API key secure.

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/VoiceLessQ/Silassaq.git
   cd Silassaq
   ```
2. Open the project in Android Studio
3. Configure the API key (see above)
4. Sync Gradle dependencies
5. Run on emulator or physical device

## Contributing

Contributions to improve Silassaq are welcome. Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- MET Norway for providing the weather data API
- GeoNames for location data
