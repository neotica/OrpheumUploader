# 🎶 Orpheum Uploader (Desktop Admin)

<img width="402" height="300" alt="image" src="https://github.com/user-attachments/assets/c06adde5-f0ba-438a-9c70-c471719c0006" />
<img width="429" height="300" alt="image" src="https://github.com/user-attachments/assets/0e38df5f-f64d-4ba0-bc58-6372779f5949" />
<img width="410" height="300" alt="image" src="https://github.com/user-attachments/assets/0a4beb5e-2dbf-484f-9b72-3b74c75d5bb4" />
<img width="413" height="300" alt="image" src="https://github.com/user-attachments/assets/80bcf735-557b-455b-8457-6dec164e62ee" />



A robust Compose Multiplatform Desktop application built to manage the **Orpheum** music streaming catalog. This admin tool handles raw audio uploads, album metadata management, and drag-and-drop cover art synchronization with our SeaweedFS bucket architecture.

The client front end app can be accessed through the following link: https://neotica.id/orpheum

## ✨ Key Features

*   **Album Catalog Management:** View all cataloged albums in a responsive, image-supported feed.
*   **Drag & Drop Cover Art:** Seamlessly drop `.png` or `.jpg` files onto an album to instantly upload them to the `orpheum` storage bucket.
*   **Metadata Editor:** Quickly edit Album Titles, Release Years, and view assigned Tracklists.
*   **Track Upload Queue:** Queue, analyze, and bulk-upload audio tracks to the server.
*   **Real-time Synchronization:** Utilizes Ktor to sync directly with the `admin-dev` backend, ensuring the Postgres database and SeaweedFS buckets are always perfectly aligned.

## 🛠️ Tech Stack

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Framework:** [Compose Multiplatform (Desktop)](https://www.jetbrains.com/lp/compose-multiplatform/)
*   **Networking:** [Ktor Client](https://ktor.io/) (for REST API & Multipart Form Data uploads)
*   **Dependency Injection:** [Koin](https://insert-koin.io/)
*   **State Management:** Kotlin Coroutines & StateFlow (MVVM Architecture)
*   **Image Loading:** [Coil 3](https://coil-kt.github.io/coil/)
