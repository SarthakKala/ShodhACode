# Quick Start Guide - Shodh-a-Code

## 🎯 Get Started in 3 Steps

### Step 1: Build Judge Image

```bash
docker build -t shodh-judge:latest ./docker/judge
```

### Step 2: Start Everything

```bash
docker-compose up --build
```

### Step 3: Open Browser

Navigate to **http://localhost:3000**

---

## 📝 Testing the Application

### Join a Contest

1. Contest ID: **1**
2. Username: **your_name**
3. Click "Join Contest"

### Solve a Problem

#### Two Sum (Java)

```java
public int[] twoSum(int[] nums, int target) {
    java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[] { map.get(complement), i };
        }
        map.put(nums[i], i);
    }
    return new int[] { 0, 0 };
}
```

#### Palindrome Number (Java)

```java
public boolean isPalindrome(int x) {
    if (x < 0) return false;
    int original = x, reversed = 0;
    while (x != 0) {
        int digit = x % 10;
        reversed = reversed * 10 + digit;
        x /= 10;
    }
    return reversed == original;
}
```

#### FizzBuzz (Java)

```java
public java.util.List<String> fizzBuzz(int n) {
    java.util.List<String> result = new java.util.ArrayList<>();
    for (int i = 1; i <= n; i++) {
        if (i % 15 == 0) result.add("FizzBuzz");
        else if (i % 3 == 0) result.add("Fizz");
        else if (i % 5 == 0) result.add("Buzz");
        else result.add(String.valueOf(i));
    }
    return result;
}
```

---

## 🔍 Verify It's Working

1. **Submit a solution** → Should show "PENDING" → "RUNNING" → "ACCEPTED"
2. **Check leaderboard** → Your name should appear with points
3. **Try different languages** → Python, C++, JavaScript all supported

---

## 🛑 Stopping

```bash
docker-compose down
```

---

## 🐛 Common Issues

| Issue                          | Solution                                                      |
| ------------------------------ | ------------------------------------------------------------- |
| Port 8080 already in use       | Stop other services or change port in docker-compose.yml      |
| Judge image not found          | Run `docker build -t shodh-judge:latest ./docker/judge`       |
| Backend can't spawn containers | Ensure Docker socket is mounted and Docker Desktop is running |
| CORS errors                    | Check backend CORS config allows your frontend origin         |

---

## 📚 More Information

- **Full Setup Guide**: See [README.md](README.md)
- **Docker Details**: See [DOCKER_SETUP.md](DOCKER_SETUP.md)
- **Backend API**: See [backend/README.md](backend/README.md)
