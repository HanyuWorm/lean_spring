# Website publishing

Website dùng **MkDocs Material + GitHub Pages**. Markdown và source code trong repository vẫn là nguồn duy nhất; `.site-docs/` chỉ được sinh tạm khi build.

## Preview local

```powershell
python -m venv .venv-docs
.\.venv-docs\Scripts\python -m pip install -r website\requirements.txt
.\.venv-docs\Scripts\python website\build_docs.py
.\.venv-docs\Scripts\python -m mkdocs serve
```

Mở `http://127.0.0.1:8000`. Build production:

```powershell
.\.venv-docs\Scripts\python website\build_docs.py
.\.venv-docs\Scripts\python -m mkdocs build --strict
```

## Publish lần đầu

1. Vào repository GitHub → **Settings → Pages**.
2. Tại **Build and deployment → Source**, chọn **GitHub Actions**.
3. Mở tab **Actions**, chạy lại workflow `Publish learning website` nếu lần chạy đầu xảy ra trước khi bật Pages.

Sau đó mọi push vào `main` sẽ tự cập nhật [hanyuworm.github.io/lean_spring](https://hanyuworm.github.io/lean_spring/).

## Phạm vi

- Render toàn bộ Markdown do learning workspace sở hữu.
- Sinh trang syntax-highlighted cho source/config của các project demo.
- Không nhân đôi source/README của các repository bên thứ ba nằm trong `11-architecture-distributed-case-studies/repositories`; chúng vẫn xem được trên GitHub.
- Không đưa file build, `.env`, dependency cache hay secret vào site.
