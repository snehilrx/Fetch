# Fetch [![Github All Releases](https://img.shields.io/github/downloads/snehilrx/Fetch/total.svg)]() [![Github All Releases](https://img.shields.io/github/issues/snehilrx/Fetch/total.svg)]() 

[![Download Apk](https://custom-icon-badges.herokuapp.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download Apk")](https://github.com/snehilrx/Fetch/releases)


<p align="center">
<img src="https://github.com/snehilrx/Fetch/assets/7668602/e1916e36-6eb3-4941-9136-0d1605286e92"/>
</p>
<p align="center">
Fetch is an open source Android client for KickassAnime, designed to provide a seamless and enjoyable anime watching experience on your mobile device. It offers a range of features including adaptive streaming, downloading anime, release notifications, skip options, offline mode, and a powerful search function.
</p>


## Features!

1. **HLS Adaptive Streaming**: Fetch utilizes HTTP Live Streaming (HLS) technology to ensure smooth playback of anime episodes, adjusting the quality based on your network conditions for an optimal viewing experience.

2. **Downloading Anime**: You can easily download anime episodes to your device for offline viewing. Fetch supports downloading in multiple qualities, allowing you to choose the option that best suits your preferences and available storage space.

3. **Anime Release Notifications**: Stay up-to-date with the latest episodes of your favorite anime. Fetch provides a notification feature that alerts you whenever a new episode is released, so you never miss a beat.

4. **Anime Skip Options**: Sometimes, you might want to skip the intro, fillers, or other parts of an episode. Fetch includes skip options that allow you to jump to specific sections, enhancing your viewing experience and saving you time.

5. **Offline Mode**: With Fetch, you can watch your downloaded anime episodes even when you don't have an internet connection. Perfect for long journeys or when you're in an area with limited connectivity.

6. **Powerful Search Function**: Searching for your favorite anime has never been easier. Fetch provides a comprehensive search feature that allows you to find specific anime series or episodes quickly.

## Installation

To use Fetch, follow these steps:

1. Clone the Fetch repository to your local machine:

   ```
   git clone https://github.com/snehilrx/Fetch.git
   ```

2. Open the project in Android Studio.

3. Add a `version.properties` file to the root directory of project with the below contents
   ```
   majorVersion=1
   minorVersion=0
   patchVersion=0
   buildNumber=3
   ```
   
4. In `local.properties` file add the following properties.
   ```
   KEY_PATH=keypath
   KEY_PASSWORD=****
   STORE_PASSWORD=***
   KEY_ALIAS=***
   ```
   For creating a signin key, read this https://developer.android.com/studio/publish/app-signing#sign-apk
      
5. Build and run the application on your Android device or emulator.

## How to Contribute

Fetch is an open source project, and contributions are welcome! If you would like to contribute to Fetch, please follow these steps:

1. Fork the repository on GitHub.

2. Create a new branch from the `dev` branch.

3. Make your changes and commit them with descriptive commit messages.

4. Push your changes to your forked repository.

5. Open a pull request against the `dev` branch of the main Fetch repository.

Please make sure to follow the project's coding style, write unit tests for new features or modifications, and update the documentation as needed.

## License

Fetch is released under the [GPLv3 License](https://raw.githubusercontent.com/snehilrx/Fetch/main/LICENSE). Feel free to use, modify, and distribute this project according to the terms of the license.

## Acknowledgments

Fetch would not have been possible without the contributions of the open source community. We would like to thank all the developers who have contributed to the project.

## Contact

If you have any questions, suggestions, or feedback, please feel free to contact us. We appreciate your input and are always happy to assist you.

Happy anime watching with Fetch!
