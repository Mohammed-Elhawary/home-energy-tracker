


# Getting Started with Home Energy Tracker
## How to start the application

first, make sure you have Docker and Docker Compose installed on your machine. Then, follow these steps:
 
1. Clone the repository to your local machine:
   ```bash
   git clone
   cd home-energy-tracker
   ```

2. Create a `.env` file in the root directory of the project and add the following environment variables:
   ```bash
    MARIADB_ROOT_PASSWORD:your_password
    MARIADB_DATABASE: home_energy_tracker
    MARIADB_USER: your_user
    MARIADB_PASSWORD: your_password
    ```

3. Start the application using Docker Compose:
   ```bash
   docker-compose -v up -d
   ```
4. The application should now be running, and you can access it at `http://localhost:8080`.

5. If you want to stop the application, run:
   ```bash
   docker-compose down
   ```

6. You can also view the logs of the application by running:
   ```bash
   docker-compose logs -f
   ```


you might have delete the pre-existing volume if you are encountering issues with the database. You can do this by running:
   ```bash  
   docker-compose down -v
   ```  
