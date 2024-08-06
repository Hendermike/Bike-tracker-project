# Bike Tracker Project

This is an Android Studio project that leverages on Google's Fused Location Provider API, Firebase Real-time Database, a Robust model-based predictive controller and ad-hoc ATMEga320p-based hardware integration to enable platoon behavior on a subscribed group of cyclists.
<br />
All relevant kinematic information is derived from GPS data and shared through the real time database to the relevant cyclists.
Acceleration predictions and robust acceleration-error intervals calculations are perfomed locally on each user's phone, leveraging a Genetic Algorithm strategy over a Quadratic-linear optimzation problem formulation and a Takagi-Sugeno model built upon historical human acceleration-error data, respectively.
Acceleration suggestions to the user are communicated both by means of 1)a change in color of a animation displayed on the phone screen, 2)and a BLE haptic interface located in the bike's handle.
